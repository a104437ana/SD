import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    private float putWorkload(long ops) {
        TestClientApp.testsStartMessage();
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
            String key = Long.toString(Math.abs(rand.nextLong() % ops));
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
        String testName = NAME + "Get";
        CsvExport csvExport = new CsvExport();
        fileName = csvExport.exportDataCsv(data, DIRECTORY, testName);
        return end.getTime() - start.getTime();
    }

    private float getWorkload(long ops) {
        TestClientApp.testsStartMessage();
        Client client = startClient(false);
        if (client == null) return -1;
        Random rand = new Random();
        byte[] value = new byte[VALUE_SIZE];
        List<String[]> data = new ArrayList<String[]>();
        String[] header = new String[] {"op", "time", "key"};
        data.add(header);

        // Inicialização dos dados no servidor
        dataInitialization(client, ops, value);

        Timestamp start = new Timestamp(System.currentTimeMillis());

        // Falta 99% dos acessos em 1% do dataset
        for (long i = 0; i < ops; i++) {
            long time = System.currentTimeMillis();
            String key = Long.toString(Math.abs(rand.nextLong() % ops));
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
        String testName = NAME + "Put";
        CsvExport csvExport = new CsvExport();
        fileName = csvExport.exportDataCsv(data, DIRECTORY, testName);
        return end.getTime() - start.getTime();
    }

    private float putGetWorkload(long ops, int ratio) {
        TestClientApp.testsStartMessage();
        Client client = startClient(false);
        if (client == null) return -1;
        Random rand = new Random();
        byte[] value = new byte[VALUE_SIZE];
        List<String[]> data = new ArrayList<String[]>();
        String[] header = new String[] {"op", "time", "key", "type"};
        data.add(header);

        // Inicialização dos dados no servidor
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
                key = Long.toString(Math.abs(rand.nextLong() % ops));
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
                key = Long.toString(Math.abs(rand.nextLong() % ops));
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
        String testName = NAME + "PutGet";
        CsvExport csvExport = new CsvExport();
        fileName = csvExport.exportDataCsv(data, DIRECTORY, testName);
        return end.getTime() - start.getTime();
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

    public static void main(String[] args) {
        TestClientApp testClient = new TestClientApp();
        if (args.length < 2 || args.length > 3) {
            TestClientApp.errorMessage("Número de argumentos inválido");
        }
        else {
            int incorrectArguments = 0;
            long ops = -1;
            int ratio = -1;
            int clients = -1;
            boolean invalidCombination = false;
            boolean type = false;
            boolean[] typeTest = new boolean[NUMBER_TESTS];
            try {
                for (int i = 0; i < args.length; i++) {
                    int test = -1;
                    if (args[i].equals("-c")) clients = Integer.parseInt(args[++i]);
                    else if (args[i].equals("-g")) test = GET;
                    else if (args[i].equals("-p")) test = PUT;
                    else if (args[i].equals("pg")) test = PUTGET;
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
                TestClientApp.errorMessage("Argumentos inválidos");
                return;
            }
            if (incorrectArguments > 0) TestClientApp.errorMessage("Argumentos inválidos");
            else if (invalidCombination) TestClientApp.errorMessage("Combinação de argumentos inválida");
            else if (!type) TestClientApp.errorMessage("Tipo de workload inválido");
            else if (ops < 0) TestClientApp.errorNumberFormat("ops");
            else if (typeTest[PUTGET] && (ratio < 0 || ratio > 100)) TestClientApp.errorNumberFormat("ratio");
            else {
                if (clients <= 0) clients = 1;
                float time = -1;
                if (typeTest[GET]) time = testClient.getWorkload(ops);
                else if (typeTest[PUT]) time = testClient.putWorkload(ops);
                else if (typeTest[PUTGET]) time = testClient.putGetWorkload(ops, ratio);
                String dirFileName = "./" + DIRECTORY + "/" + testClient.fileName;
                TestClientApp.testsEndMessage(time, dirFileName);
            }
        }
    }

    private static void errorMessage(String error) {
        System.out.println(error);
        System.out.println("  java TestClientApp <tipo de workload> <número de operações>");
        System.out.println("    Tipo de workload:");
        System.out.println("      -g : Apenas de Gets");
        System.out.println("      -p : Apenas de Puts");
        System.out.println("      -pg <int> : Puts e Gets com parâmetro para percentagem de");
        System.out.println("                  Puts em relação a Gets, de 0 a 100");
        System.out.println("    Número de operações:");
        System.out.println("      <int> : Número de operações a realizar");
    }

    private static void errorNumberFormat(String type) {
        if (type.equals("ops")) System.out.println("Número de operações inválido, deve ser um inteiro positivo");
        else if (type.equals("ratio")) System.out.println("Percentagem de Puts em relação a Gets inválida, deve ser um inteiro entre 0 e 100");
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
