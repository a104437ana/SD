import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Exit extends Message {
    private String id;
    private String tipo="Exit";

    public Exit(String id) {
        this.id = id;
    }

    public String getID() {
        return id;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeUTF(tipo);
        out.writeUTF(id);
    }

    public static Message deserialize(DataInputStream in) throws IOException {
        String id = in.readUTF();
        return new Exit(id);
    }
}
