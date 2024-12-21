import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ResLogin implements Message {
    private boolean sucessfull;
    private String tipo="ResLogin";

    public ResLogin(boolean sucessfull) {
        this.sucessfull = sucessfull;
    }


    public boolean isSucessfull() {
        return sucessfull;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeUTF(tipo);
        out.writeBoolean(sucessfull);
    }

    public static Message deserialize(DataInputStream in) throws IOException {
        boolean sucess = in.readBoolean();
        return new ResLogin(sucess);
    }


    public boolean getValue() {
        return sucessfull;
    }
}