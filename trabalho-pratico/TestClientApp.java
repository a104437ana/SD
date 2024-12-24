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
    private String fileName = "";
    private static final String USER = "test";
    private static final String PASSWORD = "test";
    private static final int NUMBER_TESTS = 3;
    private static final int GET = 0;
    private static final int PUT = 1;
    private static final int PUTGET = 2;
    private static boolean IS_MULTITHREADED = false;
    private static float maxTime = 0;
    private static ReentrantLock lock = new ReentrantLock();
    private static boolean MULTI_CLIENT = false;

    private float putWorkload(long ops, int access, long top) {
        Client client = startClient(false);
        if (client == null) return -1;
        Random rand = new Random();
        byte[] value = new byte[VALUE_SIZE];
        List<String[]> data = new ArrayList<String[]>();
        String[] header = new String[] {"op", "time", "key"};
        data.add(header);
        Timestamp start = new Timestamp(System.currentTimeMillis());

        for (long i = 0; i < ops; i++) {
            long time = System.currentTimeMillis();
            String key = Long.toString(getKey(access, top, rand, ops));
//            ///////////////////// Apenas para testar /////////////////////
//            try { Thread.sleep(rand.nextInt(50 - 20) + 20); }         ////
//            catch (InterruptedException e) { e.printStackTrace(); }   ////
//            //////////////////////////////////////////////////////////////
            client.put(key, value);
            time = System.currentTimeMillis() - time;
            String[] line = new String[] {Long.toString(i+1), Long.toString(time), key};
            data.add(line);
        }

        client.logout();
        Timestamp end = new Timestamp(System.currentTimeMillis());
        String thread = MULTI_CLIENT ? ("thread" + Long.toString(Thread.currentThread().threadId())) : "";
        String testName = NAME + "Put" + thread;
        CsvExport csvExport = new CsvExport();
        fileName = csvExport.exportDataCsv(data, DIRECTORY, testName);
        return end.getTime() - start.getTime();
    }

    private float getWorkload(long ops, int access, long top) {
        Client client = startClient(false);
        if (client == null) return -1;
        Random rand = new Random();
        byte[] value = new byte[VALUE_SIZE];
        List<String[]> data = new ArrayList<String[]>();
        String[] header = new String[] {"op", "time", "key"};
        data.add(header);

        // Inicializacao dos dados no servidor
        dataInitialization(client, ops, value);

        Timestamp start = new Timestamp(System.currentTimeMillis());

        // Falta 99% dos acessos em 1% do dataset
        for (long i = 0; i < ops; i++) {
            long time = System.currentTimeMillis();
            String key = Long.toString(getKey(access, top, rand, ops));
//            ///////////////////// Apenas para testar /////////////////////
//            try { Thread.sleep(rand.nextInt(50 - 20) + 20); }         ////
//            catch (InterruptedException e) { e.printStackTrace(); }   ////
//            //////////////////////////////////////////////////////////////
            value = client.get(key);
            time = System.currentTimeMillis() - time;
            String[] line = new String[] {Long.toString(i+1), Long.toString(time), key};
            data.add(line);
        }

        client.logout();
        Timestamp end = new Timestamp(System.currentTimeMillis());
        String thread = MULTI_CLIENT ? ("thread" + Long.toString(Thread.currentThread().threadId())) : "";
        String testName = NAME + "Get" + thread;
        CsvExport csvExport = new CsvExport();
        fileName = csvExport.exportDataCsv(data, DIRECTORY, testName);
        return end.getTime() - start.getTime();
    }

    private float putGetWorkload(long ops, int ratio, int access, long top) {
        Client client = startClient(false);
        if (client == null) return -1;
        Random rand = new Random();
        byte[] value = new byte[VALUE_SIZE];
        List<String[]> data = new ArrayList<String[]>();
        String[] header = new String[] {"op", "time", "key", "type"};
        data.add(header);

        // Inicializacao dos dados no servidor
        dataInitialization(client, ops, value);

        Timestamp start = new Timestamp(System.currentTimeMillis());

        // Falta 99% dos acessos em 1% do dataset no get
        for (long i = 0; i < ops; i++) {
            long time = System.currentTimeMillis();
            int r = rand.nextInt(100);
            String key;
            boolean isPut = r < ratio ? true : false;
            // Put
            if (isPut) {
                key = Long.toString(getKey(access, top, rand, ops));
                byte[] v = new byte[VALUE_SIZE];
//                ///////////////////// Apenas para testar /////////////////////
//                try { Thread.sleep(rand.nextInt(50 - 20) + 20); }         ////
//                catch (InterruptedException e) { e.printStackTrace(); }   ////
//                //////////////////////////////////////////////////////////////
                client.put(key, v);
            }
            // Get
            else {
                //key = Long.toString(rand.nextLong(ops));
                key = Long.toString(getKey(access, top, rand, ops));
//                key = Long.toString(Math.abs(rand.nextLong() % ops));
//                ///////////////////// Apenas para testar /////////////////////
//                try { Thread.sleep(rand.nextInt(50 - 20) + 20); }         ////
//                catch (InterruptedException e) { e.printStackTrace(); }   ////
//                //////////////////////////////////////////////////////////////
                value = client.get(key);
            }
            time = System.currentTimeMillis() - time;
            String[] line = new String[] {Long.toString(i+1), Long.toString(time), key, Boolean.toString(isPut)};
            data.add(line);
        }

        client.logout();
        Timestamp end = new Timestamp(System.currentTimeMillis());
        String thread = MULTI_CLIENT ? ("thread" + Long.toString(Thread.currentThread().threadId())) : "";
        String testName = NAME + "PutGet" + thread;
        CsvExport csvExport = new CsvExport();
        fileName = csvExport.exportDataCsv(data, DIRECTORY, testName);
        return end.getTime() - start.getTime();
    }

    private float putWorkload(long ops, int access, long top, int clients) {
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
        return maxTime;
    }

    private float getWorkload(long ops, int access, long top, int clients) {
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
        return maxTime;
    }

    private float putGetWorkload(long ops, int ratio, int access, long top, int clients) {
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
        return maxTime;
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
            if (type == GET) time = getWorkload(ops, access, top);
            else if (type == PUT) time = putWorkload(ops, access, top);
            else if (type == PUTGET) time = putGetWorkload(ops, ratio, access, top);
            lock.lock();
            try{
                if (time > maxTime) maxTime = time; 
            }
            finally {
                lock.unlock();
            }
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
        client.register(USER, PASSWORD);
        client.authenticate(USER, PASSWORD);
        return client;
    }

    private void dataInitialization(Client client, long ops, byte[] value) {
        for (long i = 0; i < ops; i++) {
            String key = Long.toString(i);
            client.put(key, value);
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
                    else if (args[i].equals("-m")) IS_MULTITHREADED = true;
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
                String dirFileName = "./" + DIRECTORY + "/" + testClient.fileName;
                TestClientApp.testsEndMessage(time, dirFileName);
            }
        }
    }

    private static void errorMessage(String error) {
        System.out.println(error);
        System.out.println("  java TestClientApp <tipo de workload> <numero de operacões> <distribuicao> <numero de clientes> <tipo do cliente>");
        System.out.println("    Tipo de workload:");
        System.out.println("      -g : Apenas de Gets");
        System.out.println("      -p : Apenas de Puts");
        System.out.println("      -pg <int> : Puts e Gets com parâmetro para percentagem de");
        System.out.println("                  Puts em relacao a Gets, de 0 a 100");
        System.out.println("    Numero de operacões:");
        System.out.println("      <int> : Numero de operacões a realizar");
        System.out.println("    Distribuicao:");
        System.out.println("      -d <int> <int> : primeiro parâmetro percentagem de acessos ao top, de 0 a 100");
        System.out.println("                       segundo parâmetro percentagem do top, de 0 a 100");
        System.out.println("                       Distribuicao por defeito 50 50, 50% acessos a 50% do dataset");
        System.out.println("    Numero de clientes:");
        System.out.println("      -c <int> : Numero de clientes singlethread ou threads de cliente multithread");
        System.out.println("                 Valor por defeito 1");
        System.out.println("    Tipo de cliente:");
        System.out.println("      -m : Cliente multithread");
        System.out.println("      -s : Cliente singlethread, valor por defeito");
    }

    private static void errorNumberFormat(String type) {
        if (type.equals("ops")) System.out.println("Numero de operacões invalido, deve ser um inteiro positivo");
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
