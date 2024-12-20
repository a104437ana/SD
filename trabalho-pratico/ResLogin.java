import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ResLogin implements Message {
    private boolean sucessfull;

    public ResLogin(boolean sucessfull) {
        this.sucessfull = sucessfull;
    }


    public boolean isSucessfull() {
        return sucessfull;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeBoolean(sucessfull);
    }

    public Message deserialize(DataInputStream in) throws IOException {
        boolean sucess = in.readBoolean();
        return new ResLogin(sucess);
    }
}