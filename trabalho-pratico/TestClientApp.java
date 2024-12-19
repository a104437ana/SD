import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestClientApp {
    
    private static final int VALUE_SIZE = 1024;
    private static final String DIRECTORY = "tmp";
    private static final String NAME = "test";

    private float putWorkload(long ops) {
        Client client = new ClientSingleThread();
        Random rand = new Random();
        byte[] value = new byte[VALUE_SIZE];
        List<String[]> data = new ArrayList<String[]>();
        String[] header = new String[] {"op", "time", "key"};
        data.add(header);
        Timestamp start = new Timestamp(System.currentTimeMillis());

        for (long i = 0; i < ops; i++) {
            long time = System.currentTimeMillis();
            String key = Long.toString(rand.nextLong(ops));
            client.put(key, value);
            time = System.currentTimeMillis() - time;
            String[] line = new String[] {Long.toString(i), Long.toString(time), key};
            data.add(line);
        }

        Timestamp end = new Timestamp(System.currentTimeMillis());
        CsvExport csvExport = new CsvExport();
        csvExport.exportDataCsv(data, DIRECTORY, NAME);
        return end.getTime() - start.getTime();
    }

    private float getWorkload(long ops) {
        Client client = new ClientSingleThread();
        Random rand = new Random();
        byte[] value = new byte[VALUE_SIZE];
        List<String[]> data = new ArrayList<String[]>();
        String[] header = new String[] {"op", "time", "key"};
        data.add(header);

        // Inicialização dos dados no servidor
        for (long i = 0; i < ops; i++) {
            long time = System.currentTimeMillis();
            String key = Long.toString(i);
            client.put(key, value);
            time = System.currentTimeMillis() - time;
            String[] line = new String[] {Long.toString(i), Long.toString(time), key};
            data.add(line);
        }

        Timestamp start = new Timestamp(System.currentTimeMillis());

        // Falta 99% dos acessos em 1% do dataset
        for (long i = 0; i < ops; i++) {
            String key = Long.toString(rand.nextLong(ops));
            value = client.get(key);
        }

        Timestamp end = new Timestamp(System.currentTimeMillis());
        CsvExport csvExport = new CsvExport();
        csvExport.exportDataCsv(data, DIRECTORY, NAME);
        return end.getTime() - start.getTime();
    }

    private float putGetWorkload(long ops, int ratio) {
        Client client = new ClientSingleThread();
        Random rand = new Random();
        byte[] value = new byte[VALUE_SIZE];
        List<String[]> data = new ArrayList<String[]>();
        String[] header = new String[] {"op", "time", "key", "type"};
        data.add(header);

        // Inicialização dos dados no servidor
        for (long i = 0; i < ops; i++) {
            String key = Long.toString(i);
            client.put(key, value);
        }

        Timestamp start = new Timestamp(System.currentTimeMillis());

        // Falta 99% dos acessos em 1% do dataset no get
        for (long i = 0; i < ops; i++) {
            long time = System.currentTimeMillis();
            int r = rand.nextInt(100);
            String key;
            boolean isPut = r < ratio ? true : false;
            // Put
            if (isPut) {
                key = Long.toString(rand.nextLong(ops));
                client.put(key, value);
            }
            // Get
            else {
                key = Long.toString(rand.nextLong(ops));
                value = client.get(key);
            }
            time = System.currentTimeMillis() - time;
            String[] line = new String[] {Long.toString(i), Long.toString(time), key, Boolean.toString(isPut)};
            data.add(line);
        }

        Timestamp end = new Timestamp(System.currentTimeMillis());
        CsvExport csvExport = new CsvExport();
        csvExport.exportDataCsv(data, DIRECTORY, NAME);
        return end.getTime() - start.getTime();
    }

    public static void main(String[] args) {
        TestClientApp testClient = new TestClientApp();
        if (args.length > 1) {
            String test = args[0];
            long ops = Long.parseLong(args[1]);
            float time;
            if (test.equals("get")) {
                time = testClient.putWorkload(ops);
            }
            else if (test.equals("put")) {
                time = testClient.getWorkload(ops);
            }
            else if (test.equals("getPut") && args.length == 3) {
                time = testClient.putGetWorkload(ops, Integer.parseInt(args[3]));
            }
        }
    }
}
