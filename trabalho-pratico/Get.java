import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Get implements Message {
    private String key;
    private String tipo="Get";


    public Get(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeUTF(tipo);
        out.writeUTF(key);
        out.flush();
    }

    public static Get deserialize(DataInputStream in) throws IOException {
        String key = in.readUTF();
        return new Get(key);
    }
}