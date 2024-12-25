import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ClientMultiThread implements Client {
    private boolean authenticated = false;
    private boolean exited = false;
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private Buffer requestBuffer = new Buffer();
    private Map<Long,Buffer> resultBuffer = new HashMap<Long,Buffer>();
    private Connection connection;
    private Dispatcher dispatcher = new Dispatcher();
    private String userId;
    private InetAddress ip;

    public ClientMultiThread() throws IOException, UnknownHostException {
        ip = InetAddress.getByName("localhost");
    }

    public boolean register(String user, String password) {
        lock.lock();
        try {
            newConnection();
            Message message = new Register(user, password);
            MessageContainer mc = new MessageContainer(message);
            connection.send(mc);
            Message res = connection.receive().getMessage();
            boolean sucessfull = false;
            if (res instanceof Response) {
                Response result = (Response) res;
                sucessfull = result.requestAccepted();
            }
            connection.close();
            return sucessfull;
        }
        finally {
            lock.unlock();
        }
    }
    public boolean authenticate(String user, String password) {
        lock.lock();
        try {
            newConnection();
            Message message = new Login(user, password);
            MessageContainer mc = new MessageContainer(message);
            connection.send(mc);
            Message res = connection.receive().getMessage();
            boolean sucessfull = false;
            if (res instanceof Response) {
                Response result = (Response) res;
                sucessfull = result.requestAccepted();
                if (sucessfull) {
                    authenticated = true;
                    userId = user;
                    dispatcher.run();
                }
                else connection.close();
            }
            return sucessfull;
        }
        finally {
            lock.unlock();
        }
    }

    public void put(String key, byte[] value) {
        if (!authenticated) return;
        long id = Thread.currentThread().threadId();
        Buffer buffer = getBuffer(id);
        Message message = new Put(key, value);
        message.setId(id);
        requestBuffer.queue(message);
        try {
            Message res = (Message) buffer.unqueue();
            if (res instanceof Success) {
                Message result = (Success) res;
                if (result != null) ;
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public byte[] get(String key) {
        if (!authenticated) return null;
        long id = Thread.currentThread().threadId();
        Buffer buffer = getBuffer(id);
        Message message = new Get(key);
        message.setId(id);
        requestBuffer.queue(message);
        byte[] value = null;
        try {
            Message res = (Message) buffer.unqueue();
            Value result = null;
            if (res instanceof Value) {
                result = (Value) res;
                value = result.getValue();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return value;
    }
    public void multiPut(Map<String,byte[]> pairs) {
        if (!authenticated) return;
        long id = Thread.currentThread().threadId();
        Buffer buffer = getBuffer(id);
        Message message = new MultiPut(pairs);
        message.setId(id);
        requestBuffer.queue(message);
        try {
            Message res = (Message) buffer.unqueue();
            if (res instanceof Success) {
                Message result = (Success) res;
                if (result != null) ;
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public Map<String,byte[]> multiGet(Set<String> keys) {
        if (!authenticated) return null;
        long id = Thread.currentThread().threadId();
        Buffer buffer = getBuffer(id);
        Message message = new MultiGet(keys);
        message.setId(id);
        requestBuffer.queue(message);
        Map<String,byte[]> pairs = null;
        try {
            Message res = (Message) buffer.unqueue();
            Values result = null;
            if (res instanceof Values) {
                result = (Values) res;
                pairs = result.getPairs();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return pairs;
    }

    public byte[] getWhen(String key, String keyCond, byte[] valueCond) {
        if (!authenticated) return null;
        long id = Thread.currentThread().threadId();
        Buffer buffer = getBuffer(id);
        Message message = new Get(key);
        message.setId(id);
        requestBuffer.queue(message);
        byte[] value = null;
        try {
            Message res = (Message) buffer.unqueue();
            Value result = null;
            if (res instanceof Value) {
                result = (Value) res;
                value = result.getValue();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return value;
    }

    public void logout() {
        if (!authenticated) return;
        authenticated = false;
        dispatcher.close();
        connection.close();
    }

    private Buffer getBuffer(long id) {
        Buffer buffer = resultBuffer.get(id);
        if (buffer == null) {
            buffer = new Buffer();
            resultBuffer.put(id,buffer);
        }
        return buffer;
    }

    class Dispatcher {
        Thread send = new Thread(new SendThread());
        Thread receive = new Thread(new ReceiveThread());
        boolean exit = false;

        private void run() {
            send.start();
            receive.start();
        }

        class SendThread implements Runnable {
            public void run() {
                while (!exit) {
                    try {
                        Message message = (Message) requestBuffer.unqueue();
                        MessageContainer mc = new MessageContainer(message);
                        connection.send(mc);
                    }
                    catch (InterruptedException e) {
                        Message message = new Exit(userId);
                        MessageContainer mc = new MessageContainer(message);
                        connection.send(mc);
                        lock.lock();
                        try {
                            exit = true;
                            condition.signalAll();
                        }
                        finally {
                            lock.unlock();
                        }
                    }
                }
            }
        }

        class ReceiveThread implements Runnable {
            public void run() {
                while (!exit) {
                    MessageContainer mc = connection.receive();
                    if (mc != null) {
                        Message message = mc.getMessage();
                        if (message != null) {
                            long id = message.getId();
                            Buffer buffer = resultBuffer.get(id);
                            buffer.queue(message);
                        }
                    }
                }
            }
        }

        private void close() {
            lock.lock();
            send.interrupt();
            receive.interrupt();
            try { while (!exit) condition.await(); }
            catch (InterruptedException e) {}
            finally { lock.unlock(); }
            try {
                send.join();
                receive.join();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void newConnection() {
        try { this.connection = new Connection(ip, 10000); }
        catch (IOException e) { e.printStackTrace(); }
    }

    public String getUserId() {
        return userId;
    }
}
