import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Get extends Message {
    private String[] key;

    Get (String[] k) {
        this.key = k;
    }

    public void serialize(DataOutputStream out) {
    }

    public Message deserialize(DataInputStream in) {
        return null;
    }
}
