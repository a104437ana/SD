import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Value extends Message {
    byte[] value;

    public Value(byte[] value){
        this.value=value;
    }
    
    public void serialize(DataOutputStream out) throws IOException{
        try {
            out.writeLong(this.getId());
            out.writeBoolean(value != null);
            if (value != null) {
                out.writeInt(value.length);
                out.write(value, 0, value.length);
            }
        } catch (IOException e) {
        }
    }

    public static Message deserialize(DataInputStream in)throws IOException {
        try{
            Value message;
            Long id = in.readLong();
            boolean b = in.readBoolean();
            if (b == true) {
                int tamanho=in.readInt();
                byte[] resposta=new byte[tamanho];
                in.read(resposta, 0, tamanho);
                message = new Value(resposta);
            }
            else message = new Value(null);
            message.setId(id);
            return message;
        }catch (IOException e){
            return null;
        }
    }

    public byte[] getValue() {
        if (value == null) return null;
        else return value.clone();
    }
}
