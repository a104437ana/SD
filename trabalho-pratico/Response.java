import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Response extends Message {
    private boolean requestAccepted;

    public Response(boolean requestAccepted) {
        this.requestAccepted = requestAccepted;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeBoolean(requestAccepted);
    }

    public static Message deserialize(DataInputStream in) throws IOException {
        boolean requestAccepted = in.readBoolean();
        return new Response(requestAccepted);
    }

    public boolean requestAccepted() {
        return requestAccepted;
    }
}