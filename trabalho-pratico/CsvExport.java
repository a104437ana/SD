import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvExport {

    private static final String SEPARATOR = ";";
    private static final String NEW_LINE = "\n";

    public void exportDataCsv(List<String[]> data, String directory, String name) {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        int id = 0;
        String fileName = name + id + ".csv";
        File file = new File(dir, fileName);
        while (file.exists()) {
            id++;
            fileName = name + id + ".csv";
            file = new File(dir, fileName);
        }

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
        csvExport.exportDataCsv(list, "tmp", "test");
    }
}
