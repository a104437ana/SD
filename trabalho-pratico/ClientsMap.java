import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClientsMap{
    Map<String,Buffer> clients;
    ReentrantReadWriteLock lock;
    Condition isFull;
    int S;
    public ClientsMap(int S){
        this.clients = new HashMap<String, Buffer>(S);
        this.lock = new ReentrantReadWriteLock();
        this.isFull = lock.writeLock().newCondition();
        this.S = S;
    }
    public void put(String id, Buffer results){
        lock.writeLock().lock();
        try{
            while(clients.size()==S){
                    isFull.await();
            }
            this.clients.put(id, results);
        } 
        catch (Exception ignore) { }
        finally{
            lock.writeLock().unlock();
        }
    }
    public void queueResult(String id, Message res){
        lock.readLock().lock();
        try{
            Buffer buffer = clients.get(id);
            if (buffer == null) return;
            buffer.queue(res);
        }
        finally{
            lock.readLock().unlock();
        }
    }
    public void remove(String id){
        lock.writeLock().lock();
        try{
            clients.remove(id);
            isFull.signal();
        } catch (Exception ignore) { }
        finally{
            lock.writeLock().unlock();
        }
    }
    public boolean isActive(String id){
        boolean res;
        lock.readLock().lock();
        try{
             res = clients.containsKey(id);
        }
        finally{
            lock.readLock().unlock();
        }
        return res;
    }
}