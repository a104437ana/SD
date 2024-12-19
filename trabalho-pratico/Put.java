import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

public class Put extends Message {
    private String[] key;
    private ArrayList<byte[]> value;

    Put (String[] k, ArrayList<byte[]> v) {
        this.key = k;
        this.value = v;
    }

    public void serialize(DataOutputStream out) {
    }

    public Message deserialize(DataInputStream in) {
        return null;
    }
}
