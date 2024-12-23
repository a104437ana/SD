import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Success extends Message {

    public Success(){
    }

    public void serialize(DataOutputStream out) throws IOException {
            try{
                out.writeLong(this.getId());
            }catch (IOException e){
                throw new IOException(e);
            }
    }

    public static Message deserialize(DataInputStream in) throws IOException {
        try{
            Long id = in.readLong();
            Success message = new Success();
            message.setId(id);
            return message;
        }catch (IOException e){
            return null;
        }
    }

}
