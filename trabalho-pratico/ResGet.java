import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ResGet implements Message {
    byte[] value;    
    
    public ResGet(byte[] value){
        this.value=value;
    }
    
    public void serialize(DataOutputStream out) throws IOException{
        try {
            out.writeInt(value.length);
            out.write(value, 0, value.length);
        } catch (IOException e) {
        }
    }

    public Message deserialize(DataInputStream in)throws IOException {
        try{
            int tamanho=in.readInt();
            byte[] resposta=new byte[tamanho];
            in.read(resposta, 0, tamanho);
            return new ResGet(resposta);
        }catch (IOException e){
            return null;
        }
    }
}
