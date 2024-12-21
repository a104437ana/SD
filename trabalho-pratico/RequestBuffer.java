import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RequestBuffer{
    final static int BUFFER_SIZE = 30;
    private Queue<Request> requestBuffer = new ArrayDeque<Request>(BUFFER_SIZE);
    private ReentrantLock lock = new ReentrantLock();
    private Condition bufferEmpty = lock.newCondition();
    private Condition bufferFull = lock.newCondition();

    public void queue(Request r){
        lock.lock();
        try{
            while(requestBuffer.size()==BUFFER_SIZE){
                bufferFull.await();
            }
            requestBuffer.add(r);
            bufferEmpty.signal();
        } 
        catch (Exception ignore) { }
        finally{
            lock.unlock();
        }
    }

    public Request unqueue(){
        Request r = null;
        lock.lock();
        try{
            while(requestBuffer.isEmpty()){
                bufferEmpty.await();
            }
            r = requestBuffer.remove();
            bufferFull.signal();
        } 
        catch (Exception ignore) { }
        finally{
            lock.unlock();
        }
        return r;
    }
}