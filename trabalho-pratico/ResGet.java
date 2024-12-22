import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ResGet extends Message {
    byte[] value;    
    private String tipo="ResGet";

    
    public ResGet(byte[] value){
        this.value=value;
    }
    
    public void serialize(DataOutputStream out) throws IOException{
        try {
            out.writeUTF(tipo);
            out.writeLong(this.getId());
            out.writeInt(value.length);
            out.write(value, 0, value.length);
        } catch (IOException e) {
        }
    }

    public static Message deserialize(DataInputStream in)throws IOException {
        try{
            Long id = in.readLong();
            int tamanho=in.readInt();
            byte[] resposta=new byte[tamanho];
            in.read(resposta, 0, tamanho);
            ResGet resGet = new ResGet(resposta);
            resGet.setId(id);
            return resGet;
        }catch (IOException e){
            return null;
        }
    }

    public byte[] getValue() {
        return this.value.clone();
    }
}
