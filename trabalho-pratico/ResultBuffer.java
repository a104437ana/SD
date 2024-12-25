import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ResultBuffer{
    private Queue<Message> resultBuffer = new LinkedList<Message>();
    private ReentrantLock lock = new ReentrantLock();
    private Condition bufferEmpty = lock.newCondition();

    public void queue(Message m){
        lock.lock();
        try{
            resultBuffer.add(m);
            bufferEmpty.signal();
        }
        finally{
            lock.unlock();
        }
    }

    public Message unqueue() throws InterruptedException{
        Message m = null;
        lock.lock();
        try{
            while(resultBuffer.isEmpty()){
                bufferEmpty.await();
            }
            m = resultBuffer.remove();
        }
        finally{
            lock.unlock();
        }
        return m;
    }

}