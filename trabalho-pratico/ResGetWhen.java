import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ResGetWhen implements Message {
    byte[] value;    
    
    public ResGetWhen(byte[] value){
        this.value=value;
    }
    
    public void serialize(DataOutputStream out) throws IOException{
        try {
            out.writeInt(value.length);  
            out.write(value);
        } catch (IOException e) {
        }
    }

    public Message deserialize(DataInputStream in)throws IOException {
        try{
            int tamanho=in.readInt();
            byte[] resposta=new byte[tamanho];
            in.readFully(resposta);
            return new ResGetWhen(resposta);
        }catch (IOException e){
            return null;
        }
    }
}
