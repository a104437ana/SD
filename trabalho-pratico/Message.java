import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Message {

    public void serialize(DataOutputStream out) throws IOException;

    public static Message deserialize(DataInputStream in) throws IOException{

        return null;
    }

}
