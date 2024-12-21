import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MultiPut implements Message{
    Map<String, byte[]> pairs;
    private String tipo="Multiput";

        public MultiPut(Map<String, byte[]> pairs){
        this.pairs=pairs;
    }

        @Override
        public void serialize(DataOutputStream out) throws IOException {
            out.writeUTF(tipo);
            out.writeInt(pairs.size());//tamanho do nosso map
            for(Map.Entry<String , byte[] > entry: pairs.entrySet()){
                String s=entry.getKey();
                byte[] b= entry.getValue();
                
                Put message=new Put(s,b);

                message.serialize(out);
            }   
        }

        public static Message deserialize(DataInputStream in) throws IOException{
            try {
               int size= in.readInt();
               Map<String, byte[]> pares=new HashMap<>();
               for(int i=0;i<size;i++){
                Put message=Put.deserialize(in);
                String chave=message.getKey();
                byte[] b=message.getValue();
                pares.put(chave, b);
               }
               return new MultiPut(pares);
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
