import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Message {

    public void serialize(DataOutputStream out) throws IOException;

    public Message deserialize(DataInputStream in) throws IOException;

}
