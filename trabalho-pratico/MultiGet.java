import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class MultiGet extends Message {
    private Set<String> keys;
    private String tipo="MultiGet";


    public MultiGet (Set<String> keys) {
        this.keys = keys;
    }

    public Set<String> getKey() {
        return keys;
    }

    public void serialize(DataOutputStream out) throws IOException {
        try{
        out.writeUTF(tipo);
        out.writeLong(this.getId());
        out.writeInt(keys.size());
        for (String key : keys) {
            out.writeUTF(key);
          }
        }catch (IOException e){
            throw new IOException(e);
        }
    
    }
    

    public static Message deserialize(DataInputStream in) throws IOException {
        Long id = in.readLong();
        int length = in.readInt();
        Set<String> keys=new HashSet<String>();
        int i=0;
        while (i<length) {
            String s=in.readUTF();
            keys.add(s);
            i++;
        }
        MultiGet multiGet = new MultiGet(keys);
        multiGet.setId(id);
        return multiGet;
    }

    public Set<String> getKeys() {
        Set<String> keySet = new HashSet<>();
        for (String s : keys) {
            keySet.add(s);
        }
        return keySet;
    }
}
