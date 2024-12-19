import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Server {
    private final int S = 10;
    private ArmazemDadosPartilhados dataBase;

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
