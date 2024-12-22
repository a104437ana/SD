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

    private float putWorkload(long ops) {
        TestClientApp.testsStartMessage();
        try {
            Client client = new ClientSingleThread();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Random rand = new Random();
        byte[] value = new byte[VALUE_SIZE];
        List<String[]> data = new ArrayList<String[]>();
        String[] header = new String[] {"op", "time", "key"};
        data.add(header);
        Timestamp start = new Timestamp(System.currentTimeMillis());

        for (long i = 0; i < ops; i++) {
            long time = System.currentTimeMillis();
            //String key = Long.toString(rand.nextLong(ops));
            String key = Long.toString(Math.abs(rand.nextLong() % ops));
            ///////////////////// Apenas para testar /////////////////////
            try { Thread.sleep(rand.nextInt(50 - 20) + 20); }         ////
            catch (InterruptedException e) { e.printStackTrace(); }   ////
            //////////////////////////////////////////////////////////////
//            client.put(key, value);
            time = System.currentTimeMillis() - time;
            String[] line = new String[] {Long.toString(i+1), Long.toString(time), key};
            data.add(line);
        }

        Timestamp end = new Timestamp(System.currentTimeMillis());
        String testName = NAME + "Get";
        CsvExport csvExport = new CsvExport();
        fileName = csvExport.exportDataCsv(data, DIRECTORY, testName);
        return end.getTime() - start.getTime();
    }

    private float getWorkload(long ops) {
        TestClientApp.testsStartMessage();
        try {
            Client client = new ClientSingleThread();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Random rand = new Random();
        byte[] value = new byte[VALUE_SIZE];
        List<String[]> data = new ArrayList<String[]>();
        String[] header = new String[] {"op", "time", "key"};
        data.add(header);

        // Inicialização dos dados no servidor
        for (long i = 0; i < ops; i++) {
            String key = Long.toString(i);
//            client.put(key, value);
        }

        Timestamp start = new Timestamp(System.currentTimeMillis());

        // Falta 99% dos acessos em 1% do dataset
        for (long i = 0; i < ops; i++) {
            long time = System.currentTimeMillis();
            //String key = Long.toString(rand.nextLong(ops));
            String key = Long.toString(Math.abs(rand.nextLong() % ops));
            ///////////////////// Apenas para testar /////////////////////
            try { Thread.sleep(rand.nextInt(50 - 20) + 20); }         ////
            catch (InterruptedException e) { e.printStackTrace(); }   ////
            //////////////////////////////////////////////////////////////
//            value = client.get(key);
            time = System.currentTimeMillis() - time;
            String[] line = new String[] {Long.toString(i+1), Long.toString(time), key};
            data.add(line);
        }

        Timestamp end = new Timestamp(System.currentTimeMillis());
        String testName = NAME + "Put";
        CsvExport csvExport = new CsvExport();
        fileName = csvExport.exportDataCsv(data, DIRECTORY, testName);
        return end.getTime() - start.getTime();
    }

    private float putGetWorkload(long ops, int ratio) {
        TestClientApp.testsStartMessage();
        try {
            Client client = new ClientSingleThread();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Random rand = new Random();
        byte[] value = new byte[VALUE_SIZE];
        List<String[]> data = new ArrayList<String[]>();
        String[] header = new String[] {"op", "time", "key", "type"};
        data.add(header);

        // Inicialização dos dados no servidor
        for (long i = 0; i < ops; i++) {
            String key = Long.toString(i);
//            client.put(key, value);
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
                //key = Long.toString(rand.nextLong(ops));
                key = Long.toString(Math.abs(rand.nextLong() % ops));
                ///////////////////// Apenas para testar /////////////////////
                try { Thread.sleep(rand.nextInt(50 - 20) + 20); }         ////
                catch (InterruptedException e) { e.printStackTrace(); }   ////
                //////////////////////////////////////////////////////////////
//                client.put(key, value);
            }
            // Get
            else {
                //key = Long.toString(rand.nextLong(ops));
                key = Long.toString(Math.abs(rand.nextLong() % ops));
                ///////////////////// Apenas para testar /////////////////////
                try { Thread.sleep(rand.nextInt(50 - 20) + 20); }         ////
                catch (InterruptedException e) { e.printStackTrace(); }   ////
                //////////////////////////////////////////////////////////////
//                value = client.get(key);
            }
            time = System.currentTimeMillis() - time;
            String[] line = new String[] {Long.toString(i+1), Long.toString(time), key, Boolean.toString(isPut)};
            data.add(line);
        }

        Timestamp end = new Timestamp(System.currentTimeMillis());
        String testName = NAME + "PutGet";
        CsvExport csvExport = new CsvExport();
        fileName = csvExport.exportDataCsv(data, DIRECTORY, testName);
        return end.getTime() - start.getTime();
    }

    public static void main(String[] args) {
        TestClientApp testClient = new TestClientApp();
        if (args.length < 2 || args.length > 3) {
            TestClientApp.errorMessage("Número de argumentos inválido");
        }
        else {
            long ops = -1;
            int ratio = -1;
            try {
                String test = args[0];
                if (args.length == 2) ops = Long.parseLong(args[1]);
                else if (args.length == 3) ops = Long.parseLong(args[2]);
                float time = -1;
                if (test.equals("-g")) time = testClient.putWorkload(ops);
                else if (test.equals("-p")) time = testClient.getWorkload(ops);
                else if (test.equals("-pg") && args.length == 3) {
                    ratio = Integer.parseInt(args[1]);
                    if (ratio >= 0 || ratio <= 100) time = testClient.putGetWorkload(ops, ratio);
                    else errorNumberFormat("ratio");
                }
                else TestClientApp.errorMessage("Argumentos inválidos");
                String dirFileName = "./" + DIRECTORY + "/" + testClient.fileName;
                TestClientApp.testsEndMessage(time, dirFileName);
            }
            catch (NumberFormatException e) {
                if (ops == -1) errorNumberFormat("ops");
                else if (ratio == -1) errorNumberFormat("ratio");
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
