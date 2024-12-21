import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GetWhen implements Message {
    private String key;
    private String keyCond;
    private byte[] valueCond;

    public GetWhen(String key, String keyCond, byte[] valueCond) {
        this.key = key;
        this.keyCond = keyCond;
        this.valueCond = valueCond;
    }

    public String getKey() {
        return key;
    }

    public String getKeyCond() {
        return keyCond;
    }

    public byte[] getValueCond() {
        return valueCond;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeUTF(key);
        out.writeUTF(keyCond);
        out.writeInt(valueCond.length);
        out.write(valueCond, 0, valueCond.length);
    }

    public Message deserialize(DataInputStream in) throws IOException {
        String key = in.readUTF();
        String keyCond = in.readUTF();
        int length = in.readInt();
        byte[] valueCond = new byte[length];
        in.read(valueCond, 0, length);
        return new GetWhen(key,keyCond,valueCond);
    }
}
