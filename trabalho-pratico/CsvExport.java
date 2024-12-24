import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class CsvExport {

    private static final String SEPARATOR = ";";
    private static final String NEW_LINE = "\n";
    private static ReentrantLock lock = new ReentrantLock();

    public String exportDataCsv(List<String[]> data, String directory, String name, boolean threaded) {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        int id = Integer.parseInt(lastId(directory, name));
        String fileName = name + id + ".csv";
        if (threaded) {
            String thread = "thread" + Long.toString(Thread.currentThread().threadId());
            fileName = name + id + thread + ".csv";
        }
        File file = new File(dir, fileName);

        try {
            BufferedWriter fw = new BufferedWriter(new FileWriter(file));

            for (String[] line : data) {
                StringBuilder sb = new StringBuilder();
                int i;
                for (i = 0; i < line.length - 1; i++) {
                    String field = line[i];
                    sb.append(field).append(SEPARATOR);
                }
                sb.append(line[i]);
                sb.append(NEW_LINE);
                String convertedLine = sb.toString();
                fw.write(convertedLine);
            }

            fw.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    private static String lastId(String dir, String fileName) {
        lock.lock();
        try {
            String lastId = "0";
            File file = new File(dir, fileName);
            try {
                if (file.exists()) {
                    BufferedReader fr = new BufferedReader(new FileReader(file));
                    String id;
                    while ((id = fr.readLine()) != null) {
                        lastId = id;
                    }
                    lastId.replace("\n", "");
                }
                else {
                    file.createNewFile();
                    BufferedWriter fw = new BufferedWriter(new FileWriter(file, true));
                    fw.write(lastId + NEW_LINE);
                    fw.flush();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return lastId;
        }
        finally {
            lock.unlock();
        }
    }

    public static void nextId(String dir, String fileName) {
        String lastId = lastId(dir, fileName);
        File file = new File(dir, fileName);
        lastId = Integer.toString(Integer.parseInt(lastId) + 1);
        try {
            BufferedWriter fw = new BufferedWriter(new FileWriter(file, true));
            fw.write(lastId + NEW_LINE);
            fw.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String[] header = {"op", "time", "type"};
        String[] line1 = {"1", "20", "0"};
        String[] line2 = {"2", "50", "1"};

        List<String[]> list = new ArrayList<>();
        list.add(header);
        list.add(line1);
        list.add(line2);

        CsvExport csvExport = new CsvExport();
//        System.out.println(csvExport.lastId("tmp", "test", false));
        csvExport.exportDataCsv(list, "tmp", "test", false);
    }
}
