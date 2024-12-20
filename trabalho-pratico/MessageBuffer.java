import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MessageBuffer{
    final static int BUFFER_SIZE = 30;
    private Queue<Message> requestBuffer = new ArrayDeque<Message>(BUFFER_SIZE);
    private ReentrantLock lock = new ReentrantLock();
    private Condition bufferEmpty = lock.newCondition();
    private Condition bufferFull = lock.newCondition();

    public void queue(Message m){
        lock.lock();
        try{
            while(requestBuffer.size()==BUFFER_SIZE){
                if(requestBuffer.size()==BUFFER_SIZE){
                    bufferFull.await();
                }
                else{
                    requestBuffer.add(m);
                    bufferEmpty.signalAll();
                    break;
                }
            }
        } catch (Exception ignore) { }
        finally{
            lock.unlock();
        }
    }

    public Message unqueue(){
        Message m = null;
        lock.lock();
        try{
            while(requestBuffer.isEmpty()){
                if(requestBuffer.isEmpty()){
                    bufferEmpty.await();
                }
                else{
                    m = requestBuffer.remove();
                    bufferFull.signalAll();
                    break;
                }
            }
        } catch (Exception ignore) { }
        finally{
            lock.unlock();
        }
        return m;
    }
}