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
        try {
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
                        sendResults.join();
                        clients.remove(e.getID());
                        break;
                    }
                    else{
                        buffer.queue(receive);
                    }
                }
            }
        } catch (Exception ignore) { }
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
        try {
            boolean connectionOpen = true;
            while(connectionOpen){
                connection.send(results.unqueue());
            }
        } catch (Exception ignore) { }
    }
}

class RequestThread implements Runnable{
    private MessageBuffer requests;
    private ClientsMap cli;
    public RequestThread(MessageBuffer buffer, ClientsMap clients){
        this.requests = buffer;
        this.cli = clients;
    }
    public void run(){ //trocar para alguma condição
        while(true){
            Message m = requests.unqueue();
            //////////////////////////////////////////////////////////////
            //processar a mensagem
            /////////////////////////////////////////////////////////////
            Message res = null;
            cli.queueResult("", res);
        }
    }
}

class ThreadPool{
    final static int THREAD_POOL_SIZE = 10;
    private Thread[] threadPool = new Thread[THREAD_POOL_SIZE];
    private MessageBuffer requestBuffer;

    private ClientsMap clients;

    public ThreadPool(ClientsMap map, MessageBuffer requests){
        this.clients = map;
        this.requestBuffer = requests;
        for (int i = 0; i<THREAD_POOL_SIZE; i++){
            threadPool[i] = new Thread(new RequestThread(requestBuffer,  clients));
            threadPool[i].run();
        }
    }
}

public class Server {
    private final static int S = 10;
    private ArmazemDadosPartilhados dataBase;

    // /**
    //  * Processa uma mensagem do tipo ... Get
    //  * @param message
    //  */
    // private void proccessMessage(Get message) {
    // }

    // /**
    //  * Processa uma mensagem do tipo ... Put
    //  * @param message
    //  */
    // private void proccessMessage(Put message) {
    // }

    // /**
    //  * Método que redireciona o processo de uma mensagem de tipo geral Message
    //  * para o método correto de processo, através do tipo concreto do objeto
    //  * @param message
    //  */
    // private void proccessMessage(Message message) {
    //     Method m = null;
    //     try {
    //         m = getClass().getMethod("processMessage", message.getClass());
    //         m.invoke(getClass(), message);
    //     }
    //     catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
    //         e.printStackTrace();
    //     }
    // }


    public static void main(String[] args){

        ClientsMap clients = new ClientsMap(S);
        AuthenticationMap credentials = new AuthenticationMap();
        MessageBuffer messages = new MessageBuffer();
        ThreadPool pool = new ThreadPool(clients, messages);

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
