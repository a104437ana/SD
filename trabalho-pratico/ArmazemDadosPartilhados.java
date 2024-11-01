import java.util.*;
import java.util.concurrent.locks.*;

class ArmazemDadosPartilhados {

    private static class Dado {
        private byte[] value;
        Lock l = new ReentrantLock();
        public Dado(byte[] value) {
            this.value = value;
        }
        public byte[] getValue() {
            return this.value;
        }
        public void setValue(byte[] value) {
            this.value = value;
        }
    }

    private Map<String,Dado> map = new HashMap<String,Dado>();
    Lock l = new ReentrantLock();

    public void put(String key, byte[] value) {
        Dado dado;
        l.lock();
        try {
            dado = map.get(key);
            if (dado == null) {
                dado = new Dado(value);
                map.put(key,value);
                return;
            }
            dado.l.lock();
        } finally {
            l.unlock();
        }
        try {
            dado.setValue(value);
        } finally {
            dado.l.unlock();
        }
    }

    public byte[] get(String key) {
        Dado dado;
        l.lock();
        try {
            dado = map.get(key);
            if (dado == null) return null;
            dado.l.lock();
        } finally {
            l.unlock();
        }
        try {
            return dado.getValue();
        } finally {
            dado.l.unlock();
        }
    }

    public void multiPut(Map<String,byte[]> pairs) {

    }

    public Map<String,byte[]> multiGet(Set<String> keys) {
        Map<String,byte[]> values = new HashMap<String,byte[]>();

    }

    public byte[] getWhen(String key, String keyCond, byte[] valueCond) {

    }
}