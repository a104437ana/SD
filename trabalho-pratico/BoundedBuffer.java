import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBuffer{
    private int BUFFER_SIZE = 30;
    private Queue<Object> buffer = new ArrayDeque<Object>(BUFFER_SIZE);
    private ReentrantLock lock = new ReentrantLock();
    private Condition bufferEmpty = lock.newCondition();
    private Condition bufferFull = lock.newCondition();

    public void queue(Object o){
        lock.lock();
        try{
            while(buffer.size()==BUFFER_SIZE){
                bufferFull.await();
            }
            buffer.add(o);
            bufferEmpty.signal();
        } 
        catch (Exception ignore) { }
        finally{
            lock.unlock();
        }
    }

    public Object unqueue(){
        Object o = null;
        lock.lock();
        try{
            while(buffer.isEmpty()){
                bufferEmpty.await();
            }
            o = buffer.remove();
            bufferFull.signal();
        } 
        catch (Exception ignore) { }
        finally{
            lock.unlock();
        }
        return o;
    }
}