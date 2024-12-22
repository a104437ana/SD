import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ResMultiGet extends Message {
    Map<String, byte[]> values;  
    private String tipo="ResMultiGet";

    public ResMultiGet(Map<String, byte[]> res){
        this.values=res;
    }
    
    public void serialize(DataOutputStream out) throws IOException{
        try {
            out.writeUTF(tipo);
            out.writeLong(this.getId());
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
            Long id = in.readLong();
            int entries = in.readInt();
            Map<String, byte[]> res = new HashMap<String,byte[]>(entries);
            for(int i = 0; i<entries; i++){
                String key = in.readUTF();
                int tamanho = in.readInt();
                byte[] value = new byte[tamanho];
                in.read(value, 0, tamanho);
                res.put(key, value);
            }
            ResMultiGet resMultiGet = new ResMultiGet(res);
            resMultiGet.setId(id);
            return resMultiGet;
        }catch (IOException e){
            return null;
        }
    }

    public Map<String,byte[]> getPairs() {
        Map<String,byte[]> pairsMap = new HashMap<>();
        for (Map.Entry<String,byte[]> e : values.entrySet()) {
            byte[] v = e.getValue();
            byte[] value = new byte[v.length];
            for (int i = 0; i < v.length; i++) {
                value[i] = v[i];
            }
            pairsMap.put(e.getKey(), value);
        }
        return pairsMap;
    }
}
