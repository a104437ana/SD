import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBuffer {
    private int BUFFER_SIZE = 30;
    private Queue<Object> buffer = new ArrayDeque<Object>(BUFFER_SIZE);
    private ReentrantLock lock = new ReentrantLock();
    private Condition bufferEmpty = lock.newCondition();
    private Condition bufferFull = lock.newCondition();

    public BoundedBuffer (int size) {
        this.BUFFER_SIZE = size;
    }

    public void queue(Object r){
        lock.lock();
        try{
            while(buffer.size()==BUFFER_SIZE){
                if(buffer.size()==BUFFER_SIZE){
                    bufferFull.await();
                }
                else{
                    buffer.add(r);
                    bufferEmpty.signalAll();
                    break;
                }
            }
        } catch (Exception ignore) { }
        finally{
            lock.unlock();
        }
    }

    public Object unqueue(){
        Object r = null;
        lock.lock();
        try{
            while(buffer.isEmpty()){
                if(buffer.isEmpty()){
                    bufferEmpty.await();
                }
                else{
                    r = buffer.remove();
                    bufferFull.signalAll();
                    break;
                }
            }
        } catch (Exception ignore) { }
        finally{
            lock.unlock();
        }
        return r;
    }
}