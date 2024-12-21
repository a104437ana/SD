import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ResMultiGet implements Message {
    Map<String, byte[]> values;  
    private String tipo="ResMultiGet";

    public ResMultiGet(Map<String, byte[]> res){
        this.values=res;
    }
    
    public void serialize(DataOutputStream out) throws IOException{
        try {
            out.writeUTF(tipo);
            out.writeInt(values.size());
            for(Map.Entry<String,byte[]> entry : values.entrySet()){
                out.writeUTF(entry.getKey());
                byte[] value = entry.getValue();
                out.writeInt(value.length);  
                out.write(value, 0, value.length);
            }
        } catch (IOException e) {
        }
    }

    public static Message deserialize(DataInputStream in)throws IOException {
        try{
            int entries = in.readInt();
            Map<String, byte[]> res = new HashMap<String,byte[]>(entries);
            for(int i = 0; i<entries; i++){
                String key = in.readUTF();
                int tamanho = in.readInt();
                byte[] value = new byte[tamanho];
                in.read(value, 0, tamanho);
                res.put(key, value);
            }
            return new ResMultiGet(res);
        }catch (IOException e){
            return null;
        }
    }
}
