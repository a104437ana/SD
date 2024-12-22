import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ResMultiPut extends Message {
    boolean sucess;
    private String tipo="ResMultiPut";


    public ResMultiPut(boolean s){
        this.sucess=s;
    }

    public void serialize(DataOutputStream out) throws IOException {
            try{
                out.writeUTF(tipo);
                out.writeLong(this.getId());
                out.writeBoolean(this.sucess);
            }catch (IOException e){
                throw new IOException(e);
            }
    }

    public static Message deserialize(DataInputStream in) throws IOException {
        try{
            Long id = in.readLong();
            boolean estado=in.readBoolean();
            ResMultiPut resMultiPut = new ResMultiPut(estado);
            resMultiPut.setId(id);
            return resMultiPut;
        }catch (IOException e){
            return null;
        }
    }
}