import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ResRegister extends Message {
    private boolean sucessfull;


    public ResRegister(boolean sucessfull) {
        this.sucessfull = sucessfull;
    }


    public boolean isSucessfull() {
        return sucessfull;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeBoolean(sucessfull);
    }

    public static Message deserialize(DataInputStream in) throws IOException {
        boolean sucess = in.readBoolean();
        return new ResRegister(sucess);
    }

    public boolean getResult() {
        return sucessfull;
    }
}