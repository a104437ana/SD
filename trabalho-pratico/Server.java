import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

class ConnectionThread implements Runnable{
    Connection connection;
    ClientsMap clients;
    AuthenticationMap credentials;
    BoundedBuffer buffer;
    Thread sendResults;
    Buffer results;
    public ConnectionThread(Connection c, ClientsMap cli, AuthenticationMap cred, BoundedBuffer buffer){
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
                MessageContainer mc = connection.receive();
                if (mc != null) {
                    Message m = mc.getMessage();
                    if(m.getClass().getSimpleName().equals("Exit")){
                        connectionOpen = false;
                        break;
                    }
                    else if(m.getClass().getSimpleName().equals("Register")){
                        Register r = (Register) m;
                        boolean sucess = credentials.register(r.getID(), r.getPassword());
                        connection.send(new MessageContainer(new Response(sucess)));
                    }
                    else if (m.getClass().getSimpleName().equals("Login")){
                        Login l = (Login) m;
                        boolean sucess = (!clients.isActive(l.getID()))&&(credentials.login(l.getID(), l.getPassword()));
                        if (sucess){
                            results = new Buffer();
                            clients.put(l.getID(), results);
                            connection.send(new MessageContainer(new Response(sucess)));
                            authenticated = true;
                            authenticatedId = l.getID();
                        }
                        else{
                            connection.send(new MessageContainer(new Response(sucess)));
                            break;
                        }
                    }
                }
                else connectionOpen = false;
            }
            if(authenticated&&connectionOpen){
                sendResults = new Thread(new ConnectionResultsThread(connection, results));
                sendResults.start();
                while(connectionOpen){
                    Message receive = connection.receive().getMessage();
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
    Buffer results;

    public ConnectionResultsThread (Connection connection, Buffer res){
        this.connection = connection;
        this.results = res;
    }

    public void run(){
        boolean connectionOpen = true;
        while(connectionOpen){
            Message m;
            try {
                m = (Message) (results.unqueue());
                MessageContainer mc = new MessageContainer(m);
                connection.send(mc);
            } 
            catch (InterruptedException e) {
                connectionOpen = false;
            }
        }
    }
}

class RequestThread implements Runnable{
    private BoundedBuffer requests;
    private ClientsMap cli;
    private ArmazemDadosPartilhados dataBase;
    public RequestThread(BoundedBuffer buffer, ClientsMap clients, ArmazemDadosPartilhados dataBase){
        this.requests = buffer;
        this.cli = clients;
        this.dataBase = dataBase;
    }
    public void run(){
        while(true){
            Request r = (Request) requests.unqueue();
            Message m = r.getMessage();
            Message res = processMessage(m);
            cli.queueResult(r.getId(), res);
        }
    }
    /**
     * Processa uma mensagem do tipo Get
     * @param message
     */
    private Message processMessage(Get message) {
        byte[] value = dataBase.get(message.getKey());
        Message res = new Value(value);
        res.setId(message.getId());
        return res;
    }

    /**
     * Processa uma mensagem do tipo Put
     * @param message
     */
    private Message processMessage(Put message) {
        dataBase.put(message.getKey(), message.getValue());
        Message res = new Success();
        res.setId(message.getId());
        return res;
    }

    /**
     * Processa uma mensagem do tipo MultiGet
     * @param message
     */
    private Message processMessage(MultiGet message) {
        Map<String,byte[]> pairs = dataBase.multiGet(message.getKeys());
        Message res = new Values(pairs);
        res.setId(message.getId());
        return res;
    }

    /**
     * Processa uma mensagem do tipo MultiPut
     * @param message
     */
    private Message processMessage(MultiPut message) {
        dataBase.multiPut(message.getPairs());
        Message res = new Success();
        res.setId(message.getId());
        return res;
    }

    /**
     * Processa uma mensagem do tipo GetWhen
     * @param message
     */
    private Message processMessage(GetWhen message) {
        byte[] value = null;
        try {
            value = dataBase.getWhen(message.getKey(), message.getKeyCond(), message.getValueCond());
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        Message res = new Value(value);
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
            m = getClass().getDeclaredMethod("processMessage", message.getClass());
            msg = (Message) m.invoke(this, message);
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
    private BoundedBuffer requestBuffer;

    private ClientsMap clients;

    public ThreadPool(ClientsMap map, BoundedBuffer requests, ArmazemDadosPartilhados dataBase){
        this.clients = map;
        this.requestBuffer = requests;
        for (int i = 0; i<THREAD_POOL_SIZE; i++){
            threadPool[i] = new Thread(new RequestThread(requestBuffer,  clients, dataBase));
            threadPool[i].start();
        }
    }
}

public class Server {        
    private static ArmazemDadosPartilhados dataBase = new ArmazemDadosPartilhados();
    public static void main(String[] args){
        int S = 1;
        if (args.length > 0) {
            String arg = args[0];
            try{
                S=Integer.parseInt(arg);
            }
            catch(NumberFormatException e){
                System.out.println("Argumento inválido");
                System.out.println("  Insira o nº máximo de clientes simultâneos");
                return;
            }
        }
        else {
            System.out.println("Numero de argumentos inválido");
            System.out.println("  java Server <nº máximo de clientes simultâneos>");
            return;
        }
        ClientsMap clients = new ClientsMap(S);
        AuthenticationMap credentials = new AuthenticationMap();
        BoundedBuffer messages = new BoundedBuffer();
        ThreadPool pool = new ThreadPool(clients, messages, Server.dataBase);

        try{
            ServerSocket ss = new ServerSocket(10000);
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
