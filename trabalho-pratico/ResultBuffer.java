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

        } catch (Exception ignore) { }
        finally{
            lock.unlock();
        }
    }

    public Message unqueue(){
        Message m = null;
        lock.lock();
        try{
            while(resultBuffer.isEmpty()){
                if(resultBuffer.isEmpty()){
                    bufferEmpty.await();
                }
                else{
                    m = resultBuffer.remove();
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