import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;

public class ClientSingleThread implements Client {
    Connection connection;
    String id;
    private InetAddress ip;

    public ClientSingleThread() throws IOException, UnknownHostException {
        ip = InetAddress.getByName("localhost");
//        this.connection = new Connection(ip, 10000);
    }

    /**
     * Método register que permite a um cliente se registar no servidor
     * @param user
     * @param password
     * @return true - sucesso / false - insucesso
     */
    public boolean register(String user, String password) {
        newConnection();
        Register r=new Register(user,password);
        MessageContainer mc = new MessageContainer(r);
        connection.send(mc); //envia a mensagem
        //procura a resposta
        Message response=connection.receive().getMessage();
        if (response instanceof Response) {
            Response res = (Response) response;
            return res.requestAccepted();
        }
        connection.close();
        return false;
    }

    /**
     * Método que permite a um cliente se autenticar no servidor
     * @param user
     * @param password
     * @return true - sucesso / false - insucesso
     */
    public boolean authenticate(String user, String password) {
        newConnection();
        Login r=new Login(user,password);
        MessageContainer mc = new MessageContainer(r);
        connection.send(mc); //envia a mensagem
        //procura a resposta
        Message response=connection.receive().getMessage();
        if (response instanceof Response) {
            Response res = (Response) response;
            boolean sucessfull = res.requestAccepted();
            if (sucessfull) id = user;
            else connection.close();
            return sucessfull;
        }
        return false;    
        }


    /**
     * Método permite a um cliente, depois de autenticado,
     * guardar um array binário na base de dados do servidor
     * @param key
     * @param value
     */
    public void put(String key, byte[] value){
        Put put=new Put(key, value);
        MessageContainer mc = new MessageContainer(put);
        connection.send(mc);
        Message response=connection.receive().getMessage();
        Success res = (Success) response;
    }

    /**
     * Método permite a um cliente, depois de autenticado,
     * aceder a um array binário guardado anteriormente na base de dados do servidor
     * @param key
     * @return
     */
    public byte[] get(String key) { 
        Get g=new Get(key);
        MessageContainer mc = new MessageContainer(g);
        connection.send(mc); //envia a mensagem
        //procura a resposta
        Message response=connection.receive().getMessage();
        Value res = (Value) response;
        return res.getValue();
    }

    /**
     * Método permite a um cliente, depois de autenticado,
     * guardar uma série de arrays binários na base de dados do servidor
     * @param pairs
     */
    public void multiPut(Map<String,byte[]> pairs) {
        MultiPut mp=new MultiPut(pairs);
        MessageContainer mc = new MessageContainer(mp);
            connection.send(mc);
            Message response=connection.receive().getMessage();
            Success res = (Success) response;
    }
    /**
     * Método permite a um cliente, depois de autenticado,
     * aceder a uma série de arrays binários guardados anteriormente na base de dados do servidor
     * @param keys
     * @return
     */
    public Map<String,byte[]> multiGet(Set<String> keys) {
        MultiGet g=new MultiGet(keys);
        MessageContainer mc = new MessageContainer(g);
        connection.send(mc); //envia a mensagem
        //procura a resposta
        Message response=connection.receive().getMessage();
        Values res = (Values) response;
        return res.getPairs();
    }

    /**
     * Método permite a um cliente, depois de autenticado,
     * aceder a um array binário guardado anteriormente na base de dados do servidor,
     * mediante uma condição e até esta ser verificada
     * @param key
     * @param keyCond
     * @param valueCond
     * @return
     */
    public byte[] getWhen(String key, String keyCond, byte[] valueCond) {
        GetWhen g=new GetWhen(key,keyCond,valueCond);
        MessageContainer mc = new MessageContainer(g);
        connection.send(mc); //envia a mensagem
        //procura a resposta
        Message response=connection.receive().getMessage();
        Value res = (Value) response;
        return res.getValue();  
    }
    
    /**
     * Método que redireciona o processo de uma mensagem de tipo geral Message
     * para o método correto de processo, através do tipo concreto do objeto
     * @param message
     */
    private void processMessage(Message message) {
        Method m = null;
        try {
            m = getClass().getMethod("processMessage", message.getClass());
            m.invoke(getClass(), message);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        Message message = new Exit(id);
        MessageContainer mc = new MessageContainer(message);
        connection.send(mc);
        connection.close();
    }

    private void newConnection() {
        try { this.connection = new Connection(ip, 10000); }
        catch (IOException e) { e.printStackTrace(); }
    }
}
