public class Request {
    private String clientId;
    private Message message;
    public Request (String id, Message req){
        this.clientId = id;
        this.message = req;
    }
    public String getId(){
        return this.clientId;
    }
    public Message getMessage(){
        return this.message;
    }
}
