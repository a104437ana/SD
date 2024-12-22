import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Buffer {
    private Queue<Object> buffer = new LinkedList<Object>();
    private ReentrantLock lock = new ReentrantLock();
    private Condition bufferEmpty = lock.newCondition();

    public void queue(Object m){
        lock.lock();
        try{
            buffer.add(m);
            bufferEmpty.signal();
        }
        finally{
            lock.unlock();
        }
    }

    public Object unqueue() throws InterruptedException{
        Object m = null;
        lock.lock();
        try{
            while(buffer.isEmpty()) {
                bufferEmpty.await();
            }
            m = buffer.remove();
        }
        finally{
            lock.unlock();
        }
        return m;
    }

}