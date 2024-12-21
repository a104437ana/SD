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
                if(requestBuffer.size()==BUFFER_SIZE){
                    bufferFull.await();
                }
                else{
                    requestBuffer.add(r);
                    bufferEmpty.signalAll();
                    break;
                }
            }
        } catch (Exception ignore) { }
        finally{
            lock.unlock();
        }
    }

    public Request unqueue(){
        Request r = null;
        lock.lock();
        try{
            while(requestBuffer.isEmpty()){
                if(requestBuffer.isEmpty()){
                    bufferEmpty.await();
                }
                else{
                    r = requestBuffer.remove();
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