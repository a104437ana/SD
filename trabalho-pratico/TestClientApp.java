import java.sql.Timestamp;
import java.util.Random;

public class TestClientApp {
    
    private static final int VALUE_SIZE = 1024;

    private float putWorkload(long ops) {
        Client client = new ClientSingleThread();
        Random rand = new Random();
        byte[] value = new byte[VALUE_SIZE];
        Timestamp start = new Timestamp(System.currentTimeMillis());

        for (int i = 0; i < ops; i++) {
            String key = Long.toString(rand.nextLong(ops));
            client.put(key, value);
        }

        Timestamp end = new Timestamp(System.currentTimeMillis());
        return end.getTime() - start.getTime();
    }

    private float getWorkload(long ops) {
        return 0;
    }

    private float putGetWorkload(long ops, int ratio) {
        return 0;
    }

    public static void main(String[] args) {
    }
}
