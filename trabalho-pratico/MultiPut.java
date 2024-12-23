import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MultiPut extends Message{
    Map<String, byte[]> pairs;

        public MultiPut(Map<String, byte[]> pairs){
        this.pairs=pairs;
    }

        @Override
        public void serialize(DataOutputStream out) throws IOException {
            out.writeLong(this.getId());
            out.writeInt(pairs.size());//tamanho do nosso map
            for(Map.Entry<String , byte[] > entry: pairs.entrySet()){
                String s=entry.getKey();
                byte[] b= entry.getValue();

                out.writeUTF(s);
                out.writeInt(b.length);
                out.write(b, 0, b.length);
            }   
        }

        public static Message deserialize(DataInputStream in) throws IOException{
            try {
               Long id = in.readLong();
               int size= in.readInt();
               Map<String, byte[]> pares=new HashMap<>();
               for(int i=0;i<size;i++){
                String chave=in.readUTF();
                int s = in.readInt();
                byte[] b = new byte[s];
                in.read(b, 0, s);
                pares.put(chave, b);
               }
               MultiPut multiPut = new MultiPut(pares);
               multiPut.setId(id);
               return multiPut;
            } catch (IOException e) {
                return null;
            }
        }

    public Map<String,byte[]> getPairs() {
        Map<String,byte[]> pairsMap = new HashMap<>();
        for (Map.Entry<String,byte[]> e : pairs.entrySet()) {
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
