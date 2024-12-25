import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class TestClientApp {
    
    private static final int VALUE_SIZE = 1024;
    private static final String DIRECTORY = "tmp";
    private static final String NAME = "test";
    private static String TESTNAME = "";
    private String fileName = "";
    private static final String USER = "client";
    private static final String PASSWORD = "test";
    private static final int NUMBER_TESTS = 3;
    private static final int GET = 0;
    private static final int PUT = 1;
    private static final int PUTGET = 2;
    private static boolean IS_MULTITHREADED = false;
    private static int MULTITHREADED_THREADS = 1;
    private static boolean DIVIDE_OPS = false;
    private static float maxTime = 0;
    private static ReentrantLock lock = new ReentrantLock();
    private static boolean MULTI_CLIENT = false;

    private float putWorkload(long ops, int access, long top, Client client) {
        if (client == null) return -1;
        Random rand = new Random();
        byte[] value = new byte[VALUE_SIZE];
        List<String[]> data = new ArrayList<String[]>();
        String[] header = new String[] {"op", "time", "key"};
        data.add(header);
        Timestamp start = new Timestamp(System.currentTimeMillis());

        for (long i = 0; i < ops; i++) {
            long time = System.nanoTime();
            String key = Long.toString(getKey(access, top, rand, ops));
            client.put(key, value);
            time = System.nanoTime() - time;
            String[] line = new String[] {Long.toString(i+1), Float.toString(((float) time) / 1000000), key};
            data.add(line);
        }

        Timestamp end = new Timestamp(System.currentTimeMillis());
        TESTNAME = NAME + "Put";
        CsvExport csvExport = new CsvExport();
        csvExport.setOptionalName(client.getUserId());
        fileName = csvExport.exportDataCsv(data, DIRECTORY, TESTNAME, MULTI_CLIENT || IS_MULTITHREADED);
        return end.getTime() - start.getTime();
    }

    private float getWorkload(long ops, int access, long top, Client client) {
        if (client == null) return -1;
        Random rand = new Random();
        byte[] value = new byte[VALUE_SIZE];
        List<String[]> data = new ArrayList<String[]>();
        String[] header = new String[] {"op", "time", "key"};
        data.add(header);

        Timestamp start = new Timestamp(System.currentTimeMillis());

        for (long i = 0; i < ops; i++) {
            long time = System.nanoTime();
            String key = Long.toString(getKey(access, top, rand, ops));
            value = client.get(key);
            time = System.nanoTime() - time;
            String[] line = new String[] {Long.toString(i+1), Float.toString(((float) time) / 1000000), key};
            data.add(line);
        }

        Timestamp end = new Timestamp(System.currentTimeMillis());
        TESTNAME = NAME + "Get";
        CsvExport csvExport = new CsvExport();
        csvExport.setOptionalName(client.getUserId());
        fileName = csvExport.exportDataCsv(data, DIRECTORY, TESTNAME, MULTI_CLIENT || IS_MULTITHREADED);
        return end.getTime() - start.getTime();
    }

    private float putGetWorkload(long ops, int ratio, int access, long top, Client client) {
        if (client == null) return -1;
        Random rand = new Random();
        byte[] value = new byte[VALUE_SIZE];
        List<String[]> data = new ArrayList<String[]>();
        String[] header = new String[] {"op", "time", "key", "type"};
        data.add(header);

        Timestamp start = new Timestamp(System.currentTimeMillis());

        for (long i = 0; i < ops; i++) {
            long time = System.nanoTime();
            int r = rand.nextInt(100);
            String key;
            boolean isPut = r < ratio ? true : false;
            // Put
            if (isPut) {
                key = Long.toString(getKey(access, top, rand, ops));
                byte[] v = new byte[VALUE_SIZE];
                client.put(key, v);
            }
            // Get
            else {
                key = Long.toString(getKey(access, top, rand, ops));
                value = client.get(key);
            }
            time = System.nanoTime() - time;
            String[] line = new String[] {Long.toString(i+1), Float.toString(((float) time) / 1000000), key, Boolean.toString(isPut)};
            data.add(line);
        }

        Timestamp end = new Timestamp(System.currentTimeMillis());
        TESTNAME = NAME + "PutGet";
        CsvExport csvExport = new CsvExport();
        csvExport.setOptionalName(client.getUserId());
        fileName = csvExport.exportDataCsv(data, DIRECTORY, TESTNAME, MULTI_CLIENT || IS_MULTITHREADED);
        return end.getTime() - start.getTime();
    }

    private float putWorkload(long ops, int access, long top, int clients) {
        Timestamp start = new Timestamp(System.currentTimeMillis());
        Thread[] threads = new Thread[clients];
        for (int i = 0; i < clients; i++) {
            threads[i] = new Thread(new Worker(PUT, ops, -1, access, top));
            threads[i].start();
        }
        try {
            for (int i = 0; i < clients; i++) {
                threads[i].join();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        Timestamp end = new Timestamp(System.currentTimeMillis());
        return end.getTime() - start.getTime();
    }

    private float getWorkload(long ops, int access, long top, int clients) {
        dataInitialization(ops);
        Timestamp start = new Timestamp(System.currentTimeMillis());
        Thread[] threads = new Thread[clients];
        for (int i = 0; i < clients; i++) {
            threads[i] = new Thread(new Worker(GET, ops, -1, access, top));
            threads[i].start();
        }
        try {
            for (int i = 0; i < clients; i++) {
                threads[i].join();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        Timestamp end = new Timestamp(System.currentTimeMillis());
        return end.getTime() - start.getTime();
    }

    private float putGetWorkload(long ops, int ratio, int access, long top, int clients) {
        dataInitialization(ops);
        Timestamp start = new Timestamp(System.currentTimeMillis());
        Thread[] threads = new Thread[clients];
        for (int i = 0; i < clients; i++) {
            threads[i] = new Thread(new Worker(PUTGET, ops, ratio, access, top));
            threads[i].start();
        }
        try {
            for (int i = 0; i < clients; i++) {
                threads[i].join();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        Timestamp end = new Timestamp(System.currentTimeMillis());
        return end.getTime() - start.getTime();
    }

    class Worker implements Runnable {
        private int type;
        private long ops;
        private int ratio;
        private int access;
        private long top;
        Worker(int t, long o, int r, int a, long top) {
            this.type = t;
            this.ops = o;
            this.ratio = r;
            this.access = a;
            this.top = top;
        }
        public void run() {
            float time = 0;
            Client client = startClient(IS_MULTITHREADED);
            if (IS_MULTITHREADED) time = runMultiThreaded(client);
            else time = runTest(client, type, ops, access, ratio, top);
            client.logout();
        }
        private float runMultiThreaded(Client client) {
            Timestamp start = new Timestamp(System.currentTimeMillis());
            Thread[] threads = new Thread[MULTITHREADED_THREADS];
            for (int i = 0; i < MULTITHREADED_THREADS; i++) {
                threads[i] = new Thread(new WorkerMultiThread(client, type, ops, access, ratio, top));
                threads[i].start();
            }
            try {
                for (int i = 0; i < MULTITHREADED_THREADS; i++) {
                    threads[i].join();
                }
            }
            catch (InterruptedException e) { e.printStackTrace(); }
            Timestamp end = new Timestamp(System.currentTimeMillis());
            return end.getTime() - start.getTime();
        }
        public float runTest(Client client, int typeWorkload, long operations, int accessRatio, int ratioType, long topOps) {
            float time = 0;
            if (typeWorkload == GET) time = getWorkload(operations, accessRatio, topOps, client);
            else if (typeWorkload == PUT) time = putWorkload(operations, accessRatio, topOps, client);
            else if (typeWorkload == PUTGET) time = putGetWorkload(operations, ratioType, accessRatio, topOps, client);
            return time;
        }
        private long[] distributeOps() {
            long operations = ops;
            long lastOperations = ops;
            if (DIVIDE_OPS) {
                operations = ops / MULTITHREADED_THREADS;
                lastOperations = operations + (ops - (operations * MULTITHREADED_THREADS));
            }
            long[] opsThreads = new long[MULTITHREADED_THREADS];
            for (int i = 0; i < (MULTITHREADED_THREADS - 1); i++) {
                opsThreads[i] = operations;
            }
            opsThreads[MULTITHREADED_THREADS - 1] = lastOperations;
            return opsThreads;
        }
        class WorkerMultiThread implements Runnable {
            private Client client;
            private long operations;
            private int accessRatio;
            private int ratioType;
            private long topOps;
            private int typeWorkload;
            WorkerMultiThread(Client c, int t, long o, int a, int r, long to) {
                this.client = c;
                this.operations = o;
                this.accessRatio = a;
                this.ratioType = r;
                this.topOps = to;
                this.typeWorkload = t;
            }
            public void run() { runTest(client, typeWorkload, operations, accessRatio, ratioType, topOps); }
        }
    }

    private Client startClient(boolean multiThreaded) {
        Client client = null;
        try {
            if (multiThreaded) client = new ClientMultiThread();
            else client = new ClientSingleThread();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        String userName = USER + Long.toString(Thread.currentThread().threadId());
        client.register(userName, PASSWORD);
        client.authenticate(userName, PASSWORD);
        return client;
    }

    private void dataInitialization(long ops) {
        try {
            byte[] value = new byte[VALUE_SIZE];
            Client client = new ClientSingleThread();
            String userName = "testDataInitialization";
            client.register(userName, PASSWORD);
            client.authenticate(userName, PASSWORD);
            for (long i = 0; i < ops; i++) {
                String key = Long.toString(i);
                client.put(key, value);
            }
            client.logout();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long getKey(int access, long top, Random rand, long ops) {
        long dataSetPercentage = Math.abs(rand.nextLong() % 100);
        long key = Math.abs(rand.nextLong());
        if (dataSetPercentage < access) key = key % top;
        else key = (key % (ops - top)) + top;
        return key;
    }

    public static void main(String[] args) {
        TestClientApp testClient = new TestClientApp();
        if (args.length < 2) {
            TestClientApp.errorMessage("Numero de argumentos invalido");
        }
        else {
            int incorrectArguments = 0;
            long ops = -1;
            int ratio = -1;
            int clients = -1;
            int access = 50;
            int top = 50;
            boolean invalidCombination = false;
            boolean type = false;
            boolean[] typeTest = new boolean[NUMBER_TESTS];
            try {
                for (int i = 0; i < args.length; i++) {
                    int test = -1;
                    if (args[i].equals("-c")) clients = Integer.parseInt(args[++i]);
                    else if (args[i].equals("-d")) { access = Integer.parseInt(args[++i]); top = Integer.parseInt(args[++i]); }
                    else if (args[i].equals("-g")) test = GET;
                    else if (args[i].equals("-p")) test = PUT;
                    else if (args[i].equals("-pg")) test = PUTGET;
                    else if (args[i].equals("-m")) { IS_MULTITHREADED = true; MULTITHREADED_THREADS = Integer.parseInt(args[++i]); }
                    else if (args[i].equals("-s")) IS_MULTITHREADED = false;
                    else incorrectArguments++;
                    if (test >= 0) {
                        typeTest[test] = true;
                        ops = Integer.parseInt(args[++i]);
                        if (test == PUTGET) ratio = Integer.parseInt(args[++i]);
                        if (type == true) invalidCombination = true;
                        else type = true;
                    }
                }
            }
            catch (NumberFormatException e) {
                TestClientApp.errorMessage("Argumentos invalidos");
                return;
            }
            if (incorrectArguments > 0) TestClientApp.errorMessage("Argumentos invalidos");
            else if (invalidCombination) TestClientApp.errorMessage("Combinacao de argumentos invalida");
            else if (!type) TestClientApp.errorMessage("Tipo de workload invalido");
            else if (ops < 0) TestClientApp.errorNumberFormat("ops");
            else if (access < 0 || access > 100) TestClientApp.errorMessage("access");
            else if (top < 0 || top > 100) TestClientApp.errorMessage("top");
            else if (typeTest[PUTGET] && (ratio < 0 || ratio > 100)) TestClientApp.errorNumberFormat("ratio");
            else {
                if (clients <= 0) clients = 1;
                if (clients > 1) MULTI_CLIENT = true;
                float time = -1;
                float topPercentage = ((float) top) / 100;
                long topOps = (long) (topPercentage * ops);
                TestClientApp.testsStartMessage();
                if (typeTest[GET]) time = testClient.getWorkload(ops, access, topOps, clients);
                else if (typeTest[PUT]) time = testClient.putWorkload(ops, access, topOps, clients);
                else if (typeTest[PUTGET]) time = testClient.putGetWorkload(ops, ratio, access, topOps, clients);
                CsvExport.nextId(DIRECTORY, TESTNAME);
                String dirFileName = "./" + DIRECTORY + "/" + testClient.fileName;
                TestClientApp.testsEndMessage(time, dirFileName);
            }
        }
    }

    private static void errorMessage(String error) {
        System.out.println(error);
        System.out.println("  java TestClientApp <tipo de workload> <numero de operacoes> <distribuicao> <numero de clientes> <tipo do cliente>");
        System.out.println("    Tipo de workload:");
        System.out.println("      -g : Apenas de Gets");
        System.out.println("      -p : Apenas de Puts");
        System.out.println("      -pg <int> : Puts e Gets com parametro para percentagem de");
        System.out.println("                  Puts em relacao a Gets, de 0 a 100");
        System.out.println("    Numero de operacoes:");
        System.out.println("      <int> : Numero de operacoes a realizar");
        System.out.println("    Distribuicao:");
        System.out.println("      -d <int> <int> : primeiro parametro percentagem de acessos ao top, de 0 a 100");
        System.out.println("                       segundo parametro percentagem do top, de 0 a 100");
        System.out.println("                       Distribuicao por defeito 50 50, 50% acessos a 50% do dataset");
        System.out.println("    Numero de clientes:");
        System.out.println("      -c <int> : Numero de clientes singlethread ou threads de cliente multithread");
        System.out.println("                 Valor por defeito 1");
        System.out.println("    Tipo de cliente:");
        System.out.println("      -m : Cliente multithread");
        System.out.println("      -s : Cliente singlethread, valor por defeito");
    }

    private static void errorNumberFormat(String type) {
        if (type.equals("ops")) System.out.println("Numero de operacoes invalido, deve ser um inteiro positivo");
        else if (type.equals("access")) System.out.println("Percentagem de acessos invalida, deve ser um inteiro entre 0 e 100");
        else if (type.equals("top")) System.out.println("Percentagem de top invalida, deve ser um inteiro entre 0 e 100");
        else if (type.equals("ratio")) System.out.println("Percentagem de Puts em relacao a Gets invalida, deve ser um inteiro entre 0 e 100");
    }

    private static void testsStartMessage() {
        System.out.println("Testes a decorrer ...");
    }

    private static void testsEndMessage(float time, String fileName) {
        if (time != -1) {
            String units = " ms";
            String decimal = "";
            if (time >= 1000 && time < 60000) {
                time /= 1000;
                units = " seg ";
                decimal = Integer.toString((int) Math.round((time % 1) * 1000)) + " ms";
            }
            else if (time >= 60000) {
                time /= 60000;
                units = " min ";
                decimal = Integer.toString((int) Math.round((time % 1) * 60)) + " seg";
            }
            System.out.println("Testes terminados com sucesso, resultados exportados para " + fileName);
            System.out.println("Tempo total decorrido: " + ((int) time) + units + decimal);
        }
    }
}
