import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public class ClientSingleThread implements Client {
    /**
     * Método register que permite a um cliente se registar no servidor
     * @param user
     * @param password
     * @return true - sucesso / false - insucesso
     */
    public boolean register(String user, String password) {
        return false;
    }

    /**
     * Método que permite a um cliente se autenticar no servidor
     * @param user
     * @param password
     * @return true - sucesso / false - insucesso
     */
    public boolean authenticate(String user, String password) {
        return false;
    }


    /**
     * Método permite a um cliente, depois de autenticado,
     * guardar um array binário na base de dados do servidor
     * @param key
     * @param value
     */
    public void put(String key, byte[] value) {
    }

    /**
     * Método permite a um cliente, depois de autenticado,
     * aceder a um array binário guardado anteriormente na base de dados do servidor
     * @param key
     * @return
     */
    public byte[] get(String key) {
        return null;
    }

    /**
     * Método permite a um cliente, depois de autenticado,
     * guardar uma série de arrays binários na base de dados do servidor
     * @param pairs
     */
    public void multiPut(Map<String,byte[]> pairs) {
    }

    /**
     * Método permite a um cliente, depois de autenticado,
     * aceder a uma série de arrays binários guardados anteriormente na base de dados do servidor
     * @param keys
     * @return
     */
    public Map<String,byte[]> multiGet(Set<String> keys) {
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
     * Processa uma mensagem do tipo ... Get
     * @param message
     */
    private void proccessMessage(Get message) {
    }

    /**
     * Processa uma mensagem do tipo ... Put
     * @param message
     */
    private void proccessMessage(Put message) {
    }

    /**
     * Método que redireciona o processo de uma mensagem de tipo geral Message
     * para o método correto de processo, através do tipo concreto do objeto
     * @param message
     */
    private void proccessMessage(Message message) {
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
