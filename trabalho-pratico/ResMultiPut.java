import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ResMultiPut implements Message {
    boolean sucess;
    private String tipo="ResMultiPut";


    public ResMultiPut(boolean s){
        this.sucess=s;
    }

    public void serialize(DataOutputStream out) throws IOException {
            try{
                out.writeUTF(tipo);
                out.writeBoolean(this.sucess);
            }catch (IOException e){
                throw new IOException(e);
            }
    }

    public static Message deserialize(DataInputStream in) throws IOException {
        try{
            boolean estado=in.readBoolean();
            return new ResMultiPut(estado);
        }catch (IOException e){
            return null;
        }
    }
}