import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ResGetWhen extends Message {
    byte[] value;
    
    public ResGetWhen(byte[] value){
        this.value=value;
    }
    
    public void serialize(DataOutputStream out) throws IOException{
        try {
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
            ResGetWhen resGetWhen = new ResGetWhen(resposta);
            resGetWhen.setId(id);
            return resGetWhen;
        }catch (IOException e){
            return null;
        }
    }

    public byte[] getValue() {
        return value.clone();
    }
}
