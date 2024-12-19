import java.util.Map;
import java.util.Set;

public class ClientMultiThread implements Client {
    public boolean register(String user, String password) {
        return false;
    }
    public boolean authenticate(String user, String password) {
        return false;
    }

    public void put(String key, byte[] value) {
    }
    public byte[] get(String key) {
        return null;
    }
    public void multiPut(Map<String,byte[]> pairs) {
    }
    public Map<String,byte[]> multiGet(Set<String> keys) {
        return null;
    }

    public byte[] getWhen(String key, String keyCond, byte[] valueCond) {
        return null;
    }
}
