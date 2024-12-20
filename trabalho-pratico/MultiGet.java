import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class MultiGet implements Message {
    private Set<String> keys;

    public MultiGet (Set<String> keys) {
        this.keys = keys;
    }

    public Set<String> getKey() {
        return keys;
    }

    public void serialize(DataOutputStream out) throws IOException {
        try{
        out.writeInt(keys.size());
        for (String key : keys) {
            Get g=new Get(key);
            g.serialize(out);
          }
        }catch (IOException e){
            throw new IOException(e);
        }
    
    }
    

    public Message deserialize(DataInputStream in) throws IOException {
        int length = in.readInt();
        Set<String> keys=new HashSet<String>();
        int i=0;
        while (i<length) {
            Get g=Get.deserialize(in);
            String s=g.getKey();
            keys.add(s);
            i++;
        }
        return new MultiGet(keys);
    }
}
