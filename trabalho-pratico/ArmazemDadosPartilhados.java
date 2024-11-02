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

    //TreeMap organiza as chaves em ordem natural, logo com ele
    //podemos implementar lock ordering para evitar deadlocks
    //TreeMap permite que qualquer string não nula seja chave
    //Se tentar usar null como chave, teremos NullPointerException
    private Map<String,Dado> map = new TreeMap<String,Dado>();
    Lock l = new ReentrantLock();

    //pré-condição: key e value não nulos
    public void put(String key, byte[] value) {
        Dado dado;
        l.lock();
        try {
            dado = map.get(key);
            if (dado == null) {
                dado = new Dado(value);
                map.put(key,dado);
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

    //pré-condição: key não nula
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
    //se a chave não existir retornamos null,
    //logo os valores não podem ser nulos
    //pois não iriamos conseguir distinguir um valor null
    //do facto de não existir chave
    //pois das duas maneiras iriamos retornar null

    //pré-condição: não tem chaves ou valores nulos
    public void multiPut(Map<String,byte[]> pairs) {
        TreeMap<String,byte[]> pairs2 = new TreeMap<String,byte[]>(pairs);
        Dado[] dado = new Dado[pairs2.size()];
        l.lock();
        int i;
        try {
            i = 0;
            for (Map.Entry<String,byte[]> pair : pairs2.entrySet()) {
                String key = pair.getKey();
                dado[i] = map.get(key);
                if (dado[i] == null) {
                    byte[] value = pair.getValue();
                    Dado d = new Dado(value);
                    map.put(key,d);
                }
                else {
                    dado[i].l.lock();
                }
                i++;
            }
        } finally {
            l.unlock();
        }
        i = 0;
        for (byte[] value : pairs2.values()) {
            if (dado[i] != null) {
                dado[i].setValue(value);
                dado[i].l.unlock();
            }
            i++;
        }
    }

    //pré-condição: não tem chaves nulas
    public Map<String,byte[]> multiGet(Set<String> keys) {
        Map<String,byte[]> pairs = new TreeMap<String,byte[]>();
        TreeSet<String> orderedKeys = new TreeSet<String>(keys);
        Dado[] dado = new Dado[orderedKeys.size()];
        l.lock();
        try {
            for (int i = 0; i < orderedKeys.size(); i++) {
                dado[i] = map.get(orderedKeys[i]); //orderedKeys é um treeset não um array
                if (dado[i] != null) dado[i].l.lock();
                //else pairs.put(orderedKeys[i],null);
            }
        } finally {
            l.unlock();
        }
        int i = 0;
        byte[] value;
        for (Dado d : dado) {
            if (d != null) {
                value = d.getValue();
                pairs.put(orderedKeys[i],value);
                d.l.unlock();
            }
            i++;
        }
        return pairs;
    }

    public byte[] getWhen(String key, String keyCond, byte[] valueCond) {

    }
}