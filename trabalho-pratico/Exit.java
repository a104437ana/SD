import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Exit implements Message {
    private String id;

    public Exit(String id) {
        this.id = id;
    }

    public String getID() {
        return id;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeUTF(id);
    }

    public Message deserialize(DataInputStream in) throws IOException {
        String id = in.readUTF();
        return new Exit(id);
    }
}
