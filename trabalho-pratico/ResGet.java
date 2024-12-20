import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ResGet implements Message {
    ArrayList<byte[]> value;    
    
    public ResGet(ArrayList<byte[]> value){
        this.value=value;
    }
    
    public void serialize(DataOutputStream out) throws IOException{
        try {
            out.writeInt(this.value.size());
            for(byte[] b : value){
                out.writeInt(b.length);  
                out.write(b);
            }
        } catch (IOException e) {
        }
    }

    public Message deserialize(DataInputStream in)throws IOException {
        try{
            ArrayList<byte[]> resposta= new ArrayList<>();
            int tamanho=in.readInt();
            int i=0;
            while(i<tamanho){
                int tamanhoB=in.readInt();
                byte[] b=new byte[tamanhoB];
                in.readFully(b);
                resposta.add(b);
            }
            return new ResGet(resposta);
        }catch (IOException e){
            return null;
        }
    }
}
