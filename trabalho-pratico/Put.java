import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Put implements Message {
    private String key;
    private byte[] value;

    public Put (String key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    public String getKey(){
        return this.key;
    }

    public byte[] getValue(){
        return this.value.clone();
    }

    public void serialize(DataOutputStream out) throws IOException{
        try{
            out.writeUTF(key);
            out.writeInt(this.value.length);
            out.write(value);
            out.flush();
        }catch (IOException e){
            throw new IOException(e);
        }
        
    }

    public static Put deserialize(DataInputStream in) throws IOException{
        
        try{
        String chave=in.readUTF();

        int tamanho=in.readInt();

        byte[] value=new byte[tamanho];
        in.readFully(value);
        
        return new Put(chave,value);
    }catch (IOException e){
        throw new IOException(e);
    }
    }
}
