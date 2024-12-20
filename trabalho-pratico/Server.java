import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class ClientsMap{
    Map<String,ResultBuffer> clients;
    ReentrantReadWriteLock lock;
    Condition isFull;
    int S;
    public ClientsMap(int S){
        this.clients = new HashMap<String, ResultBuffer>(S);
        this.lock = new ReentrantReadWriteLock();
        this.isFull = lock.writeLock().newCondition();
        this.S = S;
    }
    public void put(String id, ResultBuffer results){
        lock.writeLock().lock();
        try{
            while(clients.size()==S){
                if(clients.size()==S){
                    isFull.await();
                }
                else{
                    this.clients.put(id, results);
                    break;
                }
            }
        } catch (Exception ignore) { }
        finally{
            lock.writeLock().unlock();
        }
    }
    public void queueResult(String id, Message res){
        lock.readLock().lock();
        try{
            ResultBuffer buffer = clients.get(id);
            buffer.queue(res);
        }
        finally{
            lock.readLock().unlock();
        }
    }
    public void remove(String id){
        lock.writeLock().lock();
        try{
            clients.remove(id);
            isFull.signalAll();
        } catch (Exception ignore) { }
        finally{
            lock.writeLock().unlock();
        }
    }
}

class AuthenticationMap{
    Map<String,String> credencials;
    ReentrantReadWriteLock lock;
    public AuthenticationMap(){
        this.credencials = new HashMap<String, String>();
        this.lock = new ReentrantReadWriteLock();
    }
    public void register(String id, String password){
        lock.writeLock().lock();
        try{
            this.credencials.put(id, password);
        }
        finally{
            lock.writeLock().unlock();
        }
    }
    public boolean login(String id, String password){
        boolean res = false;
        lock.readLock().lock();
        try{
            String correctPassword = credencials.get(id);
            if(password.equals(correctPassword)) res = true;
        }
        finally{
            lock.readLock().unlock();
        }
        return res;
    }
}

class ConnectionThread implements Runnable{
    Connection connection;
    ClientsMap clients;
    AuthenticationMap credentials;
    MessageBuffer buffer;
    Thread sendResults;
    ResultBuffer results;
    public ConnectionThread(Connection c, ClientsMap cli, AuthenticationMap cred, MessageBuffer buffer){
        this.connection = c;
        this.clients = cli;
        this.credentials = cred;
        this.buffer = buffer;
    }
    public void run(){
        boolean connectionOpen = true;
            boolean authenticated = false;
            while((!authenticated)&&connectionOpen){
                Message m = connection.receive();
                if(m.getClass().getSimpleName().equals("Exit")){
                    connectionOpen = false;
                    break;
                }
                else if(m.getClass().getSimpleName().equals("Register")){
                    Register r = (Register) m;
                    credentials.register(r.getID(), r.getPassword());
                    connection.send(new ResRegister(true));
                }
                else if (m.getClass().getSimpleName().equals("Login")){
                    Login l = (Login) m;
                    results = new ResultBuffer();
                    boolean sucess = credentials.login(l.getID(), l.getPassword());
                    if (sucess){
                        clients.put(l.getID(), results);
                        connection.send(new ResLogin(sucess));
                        authenticated = true;
                    }
                    else{
                        connection.send(new ResLogin(sucess));
                        break;
                    }
                }
            }
            if(authenticated&&connectionOpen){
                sendResults = new Thread(new ConnectionResultsThread(connection, results));
                sendResults.run();
                while(connectionOpen){
                    Message receive = connection.receive();
                    if(receive.getClass().getSimpleName().equals("Exit")){
                        Exit e = (Exit) receive;
                        connectionOpen = false;
                        sendResults.interrupt();
                        clients.remove(e.getID());
                        break;
                    }
                    else{
                        buffer.queue(receive);
                    }
                }
            }
            connection.close();
    }
}

class ConnectionResultsThread implements Runnable{
    Connection connection;
    ResultBuffer results;

    public ConnectionResultsThread (Connection connection, ResultBuffer res){
        this.connection = connection;
        this.results = res;
    }

    public void run(){
        boolean connectionOpen = true;
        while(connectionOpen){
            Message m;
            try {
                m = results.unqueue();
                connection.send(m);
            } 
            catch (InterruptedException e) {
                connectionOpen = false;
            }
        }
    }
}

class RequestThread implements Runnable{
    private MessageBuffer requests;
    private ClientsMap cli;
    private ArmazemDadosPartilhados dataBase;
    public RequestThread(MessageBuffer buffer, ClientsMap clients, ArmazemDadosPartilhados dataBase){
        this.requests = buffer;
        this.cli = clients;
        this.dataBase = dataBase;
    }
    public void run(){ //trocar para alguma condição
        while(true){
            Message m = requests.unqueue();
            //////////////////////////////////////////////////////////////
            //processar a mensagem
            /////////////////////////////////////////////////////////////
            Message res = proccessMessage(m);
            cli.queueResult("", res);
        }
    }
    /**
     * Processa uma mensagem do tipo ... Get
     * @param message
     */
    private Message proccessMessage(Get message) {
        byte[] value = dataBase.get(message.getKey());
        Message res = new ResGet(value);
        return res;
    }

    /**
     * Processa uma mensagem do tipo ... Put
     * @param message
     */
    private Message proccessMessage(Put message) {
        boolean success = true;
        dataBase.put(message.getKey(), message.getValue()); // Alterar, método deve retornar um booleano ou dar trow de exception
        Message res = new ResPut(success);
        return res;
    }

    /**
     * Processa uma mensagem do tipo ... MultiGet
     * @param message
     */
    private Message proccessMessage(MultiGet message) {
        Map<String,byte[]> pairs = dataBase.multiGet(message.getKeys());
        Message res = new ResMultiGet(pairs);
        return res;
    }

    /**
     * Processa uma mensagem do tipo ... MultiPut
     * @param message
     */
    private Message proccessMessage(MultiPut message) {
        boolean success = true;
        dataBase.multiPut(message.getPairs()); // Alterar, método deve retornar um booleano ou dar trow de exception
        Message res = new ResMultiPut(success);
        return res;
    }

    /**
     * Processa uma mensagem do tipo ... GetWhen
     * @param message
     */
    private Message proccessMessage(GetWhen message) {
        byte[] value = dataBase.getWhen(message.getKey(), message.getKeyCond(), message.getValueCond()); // Alterar, método deve retornar um booleano ou dar trow de exception
        Message res = new ResGetWhen(value);
        return res;
    }

    /**
     * Método que redireciona o processo de uma mensagem de tipo geral Message
     * para o método correto de processo, através do tipo concreto do objeto
     * @param message
     */
    private Message proccessMessage(Message message) {
        Method m = null;
        Message msg = null;
        try {
            m = getClass().getMethod("processMessage", message.getClass());
            msg = (Message) m.invoke(getClass(), message);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return msg;
    }
}

class ThreadPool{
    final static int THREAD_POOL_SIZE = 10;
    private Thread[] threadPool = new Thread[THREAD_POOL_SIZE];
    private MessageBuffer requestBuffer;

    private ClientsMap clients;

    public ThreadPool(ClientsMap map, MessageBuffer requests, ArmazemDadosPartilhados dataBase){
        this.clients = map;
        this.requestBuffer = requests;
        for (int i = 0; i<THREAD_POOL_SIZE; i++){
            threadPool[i] = new Thread(new RequestThread(requestBuffer,  clients, dataBase));
            threadPool[i].run();
        }
    }
}

public class Server {
    private final static int S = 10;
    private static ArmazemDadosPartilhados dataBase = new ArmazemDadosPartilhados();

    public static void main(String[] args){

        ClientsMap clients = new ClientsMap(S);
        AuthenticationMap credentials = new AuthenticationMap();
        MessageBuffer messages = new MessageBuffer();
        ThreadPool pool = new ThreadPool(clients, messages, Server.dataBase);

        try{
            ServerSocket ss = new ServerSocket(10000);
            boolean server_open = true;
            while(server_open) {
                Socket s = ss.accept();
                Connection c = new Connection(s);
                Thread t = new Thread(new ConnectionThread(c, clients, credentials, messages));
                t.start();
            }
            ss.close();
        }
        catch (Exception ignore){}
    }
}
