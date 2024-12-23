import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Register extends Message {
    private String id;
    private String password;


    public Register(String id, String password) {
        this.id = id;
        this.password = password;
    }

    public String getID() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeUTF(id);
        out.writeUTF(password);
    }

    public static Message deserialize(DataInputStream in) throws IOException {
        String id = in.readUTF();
        String password = in.readUTF();
        return new Register(id, password);
    }
}
