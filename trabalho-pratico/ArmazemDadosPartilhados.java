import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.TreeMap;

class ArmazemDadosPartilhados {

    private static class Dado {
        private byte[] value;
        Lock l = new ReentrantLock();
        Condition c = l.newCondition();
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
    Condition c = l.newCondition();

    //pré-condição: key e value não nulos
    public void put(String key, byte[] value) {
        Dado dado;
        l.lock();
        try {
            dado = map.get(key);
            if (dado == null) {
                dado = new Dado(value);
                map.put(key,dado);
                c.signalAll();
                return;
            }
            dado.l.lock();
        } finally {
            l.unlock();
        }
        try {
            dado.setValue(value);
            dado.c.signalAll();
        } finally {
            dado.l.unlock();
        }
    }

    //pré-condição: key não nula
    public byte[] get(String key) {
        byte [] value;
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
            value = dado.getValue();
        } finally {
            dado.l.unlock();
        }
        return value;
    }
    //se a chave não existir retornamos null,
    //logo os valores não podem ser nulos
    //pois não iriamos conseguir distinguir um valor null
    //do facto de não existir chave
    //pois das duas maneiras iriamos retornar null

    //pré-condição: não tem chaves nem valores nulos
    public void multiPut(Map<String,byte[]> pairs) {
        Map<String,byte[]> orderedPairs = new TreeMap<String,byte[]>(pairs);
        List<Dado> dados = new ArrayList<Dado>();
        List<byte[]> values = new ArrayList<byte[]>();
        l.lock();
        Dado dado;
        String key;
        byte[] value;
        try {
            for (Map.Entry<String,byte[]> pair : orderedPairs.entrySet()) {
                key = pair.getKey();
                value = pair.getValue();
                dado = map.get(key);
                if (dado == null) {
                    dado = new Dado(value);
                    map.put(key,dado);
                    c.signalAll();
                }
                else {
                    dados.add(dado);
                    values.add(value);
                    dado.l.lock();
                }
            }
        } finally {
            l.unlock();
        }
        for (int i = 0; i < dados.size(); i++) {
            dado = dados.get(i);
            value = values.get(i);
            dado.setValue(value);
            dado.c.signalAll();
            dado.l.unlock();
        }
    }

    //pré-condição: não tem chaves nulas
    public Map<String,byte[]> multiGet(Set<String> keys) {
        Map<String,byte[]> pairs = new TreeMap<String,byte[]>();
        Set<String> orderedKeys = new TreeSet<String>(keys);
        List<Dado> dados = new ArrayList<Dado>();
        List<String> keysWithDados = new ArrayList<String>();
        l.lock();
        Dado dado;
        byte[] value;
        try {
            for (String key : orderedKeys) {
                dado = map.get(key);
                if (dado != null) {
                    dados.add(dado);
                    keysWithDados.add(key);
                    dado.l.lock();
                }
                //else pairs.put(key,null);
            }
        } finally {
            l.unlock();
        }
        for (int i = 0; i < dados.size(); i++) {
            dado = dados.get(i);
            value = dado.getValue();
            String key = keysWithDados.get(i);
            pairs.put(key,value);
            dado.l.unlock();
        }
        return pairs;
    }

    public byte[] getWhen(String key, String keyCond, byte[] valueCond) throws InterruptedException {
        Dado dado;
        l.lock();
        try {
            //if (dado == null) return null;
            while ((dado = map.get(keyCond)) == null) {
                c.await();
            }
            dado.l.lock();
        } finally {
            l.unlock();
        }
        try {
          while (!(dado.getValue().equals(valueCond))) {
            dado.c.await();
          }
        } finally {
            dado.l.unlock();
        }
        return get(key);
    }
}