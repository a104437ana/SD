import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Put implements Message {
    private String key;
    private byte[] value;

    public Put (String key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    public void serialize(DataOutputStream out) {
    }

    public Message deserialize(DataInputStream in) {
        return null;
    }
}
