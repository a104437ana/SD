import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class Message {
    public void serialize(DataOutputStream out) {
    }

    public Message deserialize(DataInputStream in) {
        return null;
    }
}
