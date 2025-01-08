import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Message {
    private long id = -1;

    public abstract void serialize(DataOutputStream out) throws IOException;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }
}
