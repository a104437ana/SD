import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Logout implements Message {
    private String id;

    public Logout(String id) {
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
        return new Logout(id);
    }
}
