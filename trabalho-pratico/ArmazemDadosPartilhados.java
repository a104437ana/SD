import java.util.*;
import java.util.concurrent.locks.*;

class ArmazemDadosPartilhados {

    private static class Dado {
        private byte[] value;
        Lock l = new ReentrantLock();
        Dado (byte[] value) {
            this.value = value;
        }
        byte[] value() {
            return this.value;
        }
    }

    private Map<String,Dado> map = new HashMap<String,Dado>();
    Lock l = new ReentrantLock();

    public void put(String key, byte[] value) {
        
    }

    public byte[] get (String key) {
        Dado dado = map.get(key);
        if (dado == null) return null;
        else {
            dado.l.lock();
            try {
                return dado.value();
            } finally {
                dado.l.unlock();
            }
        }
    }

    public void multiPut (Map<String,byte[]> pairs) {

    }

    public Map<String,byte[]> multiGet (Set<String> keys) {

    }
}