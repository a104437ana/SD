import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Get extends Message {
    private String key;

    public Get(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeLong(this.getId());
        out.writeUTF(key);
    }

    public static Message deserialize(DataInputStream in) throws IOException {
        Long id = in.readLong();
        String key = in.readUTF();
        Get get = new Get(key);
        get.setId(id);
        return get;
    }
}