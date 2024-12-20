import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ResMultiPut implements Message {
    boolean sucess;

    public ResMultiPut(boolean s){
        this.sucess=s;
    }

    public void serialize(DataOutputStream out) throws IOException {
            try{
                out.writeBoolean(this.sucess);
            }catch (IOException e){
                throw new IOException(e);
            }
    }

    public Message deserialize(DataInputStream in) throws IOException {
        try{
            boolean estado=in.readBoolean();
            return new ResMultiPut(estado);
        }catch (IOException e){
            return null;
        }
    }
}