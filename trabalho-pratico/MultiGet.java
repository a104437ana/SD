import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

public class MultiGet implements Message {
    private Set<String> keys;

    public MultiGet (Set<String> keys) {
        this.keys = keys;
    }

    public Set<String> getKey() {
        return keys;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(keys.size());
        for (String key : keys) {
            out.writeUTF(key);
        }
    }

    public Message deserialize(DataInputStream in) throws IOException {
        int length = in.readInt();
        return null;
    }
}
