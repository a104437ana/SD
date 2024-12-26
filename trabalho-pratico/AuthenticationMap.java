import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AuthenticationMap{
    Map<String,String> credentials;
    ReentrantReadWriteLock lock;
    public AuthenticationMap(){
        this.credentials = new HashMap<String, String>();
        this.lock = new ReentrantReadWriteLock();
    }
    public boolean register(String id, String password){
        lock.writeLock().lock();
        try{
            if(this.credentials.containsKey(id)) return false;
            else{
                this.credentials.put(id, password);
                return true;
            }
        }
        finally{
            lock.writeLock().unlock();
        }
    }
    public boolean login(String id, String password){
        boolean res = false;
        lock.readLock().lock();
        try{
            String correctPassword = credentials.get(id);
            if(password.equals(correctPassword)) res = true;
        }
        finally{
            lock.readLock().unlock();
        }
        return res;
    }
}