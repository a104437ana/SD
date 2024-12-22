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
                    isFull.await();
            }
            this.clients.put(id, results);
        } 
        catch (Exception ignore) { }
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
            isFull.signal();
        } catch (Exception ignore) { }
        finally{
            lock.writeLock().unlock();
        }
    }
}

class AuthenticationMap{
    Map<String,String> credentials;
    ReentrantReadWriteLock lock;
    public AuthenticationMap(){
        this.credentials = new HashMap<String, String>();
        this.lock = new ReentrantReadWriteLock();
    }
    public void register(String id, String password){
        lock.writeLock().lock();
        try{
            this.credentials.put(id, password);
        }
        finally{
            lock.writeLock().unlock();
        }
    }
    public boolean login(String id, String password){
        boolean res = false;
        lock.readLock().lock();
        try{
            String correctPassword = credentials.get(id);
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
    RequestBuffer buffer;
    Thread sendResults;
    ResultBuffer results;
    public ConnectionThread(Connection c, ClientsMap cli, AuthenticationMap cred, RequestBuffer buffer){
        this.connection = c;
        this.clients = cli;
        this.credentials = cred;
        this.buffer = buffer;
    }
    public void run(){
        boolean connectionOpen = true;
            boolean authenticated = false;
            String authenticatedId = null;
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
                        authenticatedId = l.getID();
                    }
                    else{
                        connection.send(new ResLogin(sucess));
                        break;
                    }
                }
            }
            if(authenticated&&connectionOpen){
                sendResults = new Thread(new ConnectionResultsThread(connection, results));
                sendResults.start();
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
                        Request r = new Request(authenticatedId, receive);
                        buffer.queue(r);
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
    private RequestBuffer requests;
    private ClientsMap cli;
    private ArmazemDadosPartilhados dataBase;
    public RequestThread(RequestBuffer buffer, ClientsMap clients, ArmazemDadosPartilhados dataBase){
        this.requests = buffer;
        this.cli = clients;
        this.dataBase = dataBase;
    }
    public void run(){ //trocar para alguma condição
        while(true){
            Request r = requests.unqueue();
            Message m = r.getMessage();
            Message res = processMessage(m);
            cli.queueResult(r.getId(), res);
        }
    }
    /**
     * Processa uma mensagem do tipo ... Get
     * @param message
     */
    private Message processMessage(Get message) {
        byte[] value = dataBase.get(message.getKey());
        Message res = new ResGet(value);
        res.setId(message.getId());
        return res;
    }

    /**
     * Processa uma mensagem do tipo ... Put
     * @param message
     */
    private Message processMessage(Put message) {
        boolean success = true;
        dataBase.put(message.getKey(), message.getValue()); // Alterar, método deve retornar um booleano ou dar trow de exception
        Message res = new ResPut(success);
        res.setId(message.getId());
        return res;
    }

    /**
     * Processa uma mensagem do tipo ... MultiGet
     * @param message
     */
    private Message processMessage(MultiGet message) {
        Map<String,byte[]> pairs = dataBase.multiGet(message.getKeys());
        Message res = new ResMultiGet(pairs);
        res.setId(message.getId());
        return res;
    }

    /**
     * Processa uma mensagem do tipo ... MultiPut
     * @param message
     */
    private Message processMessage(MultiPut message) {
        boolean success = true;
        dataBase.multiPut(message.getPairs()); // Alterar, método deve retornar um booleano ou dar trow de exception
        Message res = new ResMultiPut(success);
        res.setId(message.getId());
        return res;
    }

    /**
     * Processa uma mensagem do tipo ... GetWhen
     * @param message
     */
    private Message processMessage(GetWhen message) {
        byte[] value = null;
        try {
            value = dataBase.getWhen(message.getKey(), message.getKeyCond(), message.getValueCond()); // Alterar, método deve retornar um booleano ou dar trow de exception
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        Message res = new ResGetWhen(value);
        res.setId(message.getId());
        return res;
    }

    /**
     * Método que redireciona o processo de uma mensagem de tipo geral Message
     * para o método correto de processo, através do tipo concreto do objeto
     * @param message
     */
    private Message processMessage(Message message) {
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
    private RequestBuffer requestBuffer;

    private ClientsMap clients;

    public ThreadPool(ClientsMap map, RequestBuffer requests, ArmazemDadosPartilhados dataBase){
        this.clients = map;
        this.requestBuffer = requests;
        System.out.println("A criar " + THREAD_POOL_SIZE + " threads"); // Para apagar
        for (int i = 0; i<THREAD_POOL_SIZE; i++){
            threadPool[i] = new Thread(new RequestThread(requestBuffer,  clients, dataBase));
            System.out.println("Criada thread " + i); // Para apagar
            threadPool[i].start();
            System.out.println("Iniciada thread " + i); // Para apagar
        }
    }
}

public class Server {
    private final static int S = 10;
    private static ArmazemDadosPartilhados dataBase = new ArmazemDadosPartilhados();

    public static void main(String[] args){

        ClientsMap clients = new ClientsMap(S);
        AuthenticationMap credentials = new AuthenticationMap();
        RequestBuffer messages = new RequestBuffer();
        System.out.println("Inicializar thread pool"); // Para apagar
        ThreadPool pool = new ThreadPool(clients, messages, Server.dataBase);
        System.out.println("Thread pool acabou"); // Para apagar

        try{
            ServerSocket ss = new ServerSocket(10001);
            System.out.println("Server conectado na porta " + ss.getLocalPort()); // Para apagar
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
