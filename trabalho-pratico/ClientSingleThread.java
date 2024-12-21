import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Map;
import java.util.Set;

public class ClientSingleThread implements Client {
    Socket socket;
    Connection connection;
    //Cliente id;

    public ClientSingleThread(Socket socket) throws IOException{
        this.socket=socket;
        this.connection=new Connection(socket);
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
            return res.getValue();
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
            return res.getValue();
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
        if (response instanceof ResGet) {
            ResGet res = (ResGet) response;
            return res.getValue();
        }
        return null;
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
            if (response instanceof MultiPut) {
                MultiPut res = (MultiPut) response;
                //return res.getPairs();
        }
    }
    /**
     * Método permite a um cliente, depois de autenticado,
     * aceder a uma série de arrays binários guardados anteriormente na base de dados do servidor
     * @param keys
     * @return
     */
    public ResMultiGet multiGet(Set<String> keys) {
        MultiGet g=new MultiGet(keys);
        connection.send(g); //envia a mensagem
        //procura a resposta
        Message response=connection.receive();
        if (response instanceof ResMultiGet) {
            ResMultiGet res = (ResMultiGet) response;
            return res;
        }
        return null;
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
        return null;
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
}
