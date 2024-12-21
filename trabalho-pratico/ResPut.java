import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ResPut implements Message {
    boolean sucess;
    private String tipo="ResPut";

    public ResPut(boolean s){
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
            return new ResPut(estado);
        }catch (IOException e){
            return null;
        }
    }
}
