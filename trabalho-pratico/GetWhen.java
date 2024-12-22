import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GetWhen extends Message {
    private String key;
    private String keyCond;
    private byte[] valueCond;
    private String tipo="GetWhen";

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
        out.writeUTF(tipo);
        out.writeLong(this.getId());
        out.writeUTF(key);
        out.writeUTF(keyCond);
        out.writeInt(valueCond.length);
        out.write(valueCond, 0, valueCond.length);
    }

    public static Message deserialize(DataInputStream in) throws IOException {
        Long id = in.readLong();
        String key = in.readUTF();
        String keyCond = in.readUTF();
        int length = in.readInt();
        byte[] valueCond = new byte[length];
        in.read(valueCond, 0, length);
        GetWhen getWhen = new GetWhen(key, keyCond, valueCond);
        getWhen.setId(id);
        return getWhen;
    }
}
