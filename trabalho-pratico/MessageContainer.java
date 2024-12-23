import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class MessageContainer {
    private MessageType messageType;
    private Message message;

    public MessageContainer(Message message) {
        this.messageType = MessageType.valueOf(message.getClass().getSimpleName());
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(messageType.ordinal());
        message.serialize(out);
    }

    public static MessageContainer deserialize(DataInputStream in) throws Exception {
        int ordinalValue = in.readInt();
        MessageType messageType = MessageType.values()[ordinalValue];
        String messageTypeName = messageType.name();
        Class<?> c = Class.forName(messageTypeName);
        Message message = (Message) c.getMethod("deserialize",DataInputStream.class).invoke(null,in);
        return new MessageContainer(message);
    }
}
