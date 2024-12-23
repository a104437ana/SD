import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

public class ClientSingleThread implements Client {
    Connection connection;
    String id;

    public ClientSingleThread() throws IOException, UnknownHostException {
        InetAddress ip = InetAddress.getByName("localhost");
        this.connection = new Connection(ip, 10000);
    }

    /**
     * Método register que permite a um cliente se registar no servidor
     * @param user
     * @param password
     * @return true - sucesso / false - insucesso
     */
    public boolean register(String user, String password) {
        Register r=new Register(user,password);
        connection.send(r); //envia a mensagem
        //procura a resposta
        Message response=connection.receive();
        if (response instanceof ResRegister) {
            ResRegister res = (ResRegister) response;
            return res.getResult();
        }
        return false;
    }

    /**
     * Método que permite a um cliente se autenticar no servidor
     * @param user
     * @param password
     * @return true - sucesso / false - insucesso
     */
    public boolean authenticate(String user, String password) {
        Login r=new Login(user,password);
        connection.send(r); //envia a mensagem
        //procura a resposta
        Message response=connection.receive();
        if (response instanceof ResLogin) {
            ResLogin res = (ResLogin) response;
            boolean sucessfull = res.getResult();
            if (sucessfull) id = user;
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
        connection.send(put);
        Message response=connection.receive();
        ResPut res = (ResPut) response;
    }

    /**
     * Método permite a um cliente, depois de autenticado,
     * aceder a um array binário guardado anteriormente na base de dados do servidor
     * @param key
     * @return
     */
    public byte[] get(String key) { 
        Get g=new Get(key);
        connection.send(g); //envia a mensagem
        //procura a resposta
        Message response=connection.receive();
        ResGet res = (ResGet) response;
        return res.getValue();
    }

    /**
     * Método permite a um cliente, depois de autenticado,
     * guardar uma série de arrays binários na base de dados do servidor
     * @param pairs
     */
    public void multiPut(Map<String,byte[]> pairs) {
        MultiPut mp=new MultiPut(pairs);
            connection.send(mp);
            Message response=connection.receive();
            ResMultiPut res = (ResMultiPut) response;
    }
    /**
     * Método permite a um cliente, depois de autenticado,
     * aceder a uma série de arrays binários guardados anteriormente na base de dados do servidor
     * @param keys
     * @return
     */
    public Map<String,byte[]> multiGet(Set<String> keys) {
        MultiGet g=new MultiGet(keys);
        connection.send(g); //envia a mensagem
        //procura a resposta
        Message response=connection.receive();
        ResMultiGet res = (ResMultiGet) response;
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
        connection.send(g); //envia a mensagem
        //procura a resposta
        Message response=connection.receive();
        ResGetWhen res = (ResGetWhen) response;
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
        connection.send(message);
        connection.close();
    }
}
