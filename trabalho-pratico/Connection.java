import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe Connection responsável por enviar Messages da origem ao destino,
 * através de uma socket tcp
 * Utiliza Data Input/Output Stream para serializar e deserializar Messages
 */
public class Connection {
    private Socket socket;
    private DataInputStream in;
    private ReentrantLock lockIn;    
    private DataOutputStream out;
    private ReentrantLock lockOut;
    private ResultBuffer results;
    private RequestBuffer requests;

    /**
     * Construtor parametrizado que cria um objeto Connection
     * com uma socket conectada a um ip e uma port
     * @param ip ip do destinatário
     * @param port porta do destinatário
     * @throws IOException quando falhar ao criar a Socket ou Data Input/Output Streams
     */
    Connection (InetAddress ip, int port) throws IOException {
        this.socket = new Socket(ip, port);        
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.lockIn = new ReentrantLock();
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.lockOut = new ReentrantLock();
        this.results=new ResultBuffer();
        this.requests=new RequestBuffer();
    }

    /**
     * Construtor parametrizado que cria um objeto Connection
     * com a uma socket previamente conectada
     * @param s socket conectada a um destinatário
     * @throws IOException quando falhar ao criar a Data Input/Output Streams
     */
    Connection (Socket s) throws IOException {
        this.socket = s;
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.lockIn = new ReentrantLock();
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.lockOut = new ReentrantLock();
    }

    /**
     * Método que permite enviar uma Message ao destinatário,
     * por tcp, a qual a socket se encontra conectada
     * @param m
     */
    public void send(Message m)  throws IOException{
        lockOut.lock();
            try {
                m.serialize(out);
                
            } catch (IOException e) {
            }finally{
                lockOut.unlock();
            }
    }

    /**
     * Método que permite receber uma Message do destinatário,
     * por tcp, a qual a socket se encontra conectada
     * @return
     */
    public Message receive(String id) throws IOException {
        lockIn.lock();
        try{
            String tipo = in.readUTF();
            Message message;
            switch (tipo) {
                case "Put":
                    message = Put.deserialize(in);
                    Request request=new Request(id, message);
                    requests.queue(request);
                    return message;
                case "ResPut":
                    message = ResPut.deserialize(in);
                    results.queue(message);
                    return message;  
                case "Get":
                    message = Get.deserialize(in);
                    Request request2=new Request(id, message);
                    requests.queue(request2);
                    return message;
                case "ResGet":
                    message = ResGet.deserialize(in);
                    results.queue(message);
                    return message; 
                case "MultiPut":
                    message = MultiPut.deserialize(in);
                    Request request3=new Request(id, message);
                    requests.queue(request3);                      
                    return message; 
                case "ResMultiPut":
                    message = ResMultiPut.deserialize(in);
                    results.queue(message);
                    return message;
                case "MultiGet":
                    message = MultiGet.deserialize(in);
                    Request request4=new Request(id, message);
                    requests.queue(request4);           
                    return message;
                case "ResMultiGet":
                    message = ResMultiGet.deserialize(in);
                    results.queue(message);
                    return message;
                case "Exit":
                    message = Exit.deserialize(in);
                    Request request5=new Request(id, message);
                    requests.queue(request5);
                    return message;
                case "Login":
                    message = Login.deserialize(in);
                    Request request6=new Request(id, message);
                    requests.queue(request6);
                    return message;
                case "ResLogin":
                    message = ResLogin.deserialize(in);
                    results.queue(message);
                    return message;
                case "Register":
                    message = Register.deserialize(in);
                    Request request7=new Request(id, message);
                    requests.queue(request7);                    
                    return message;
                case "ResRegister":
                    message = ResRegister.deserialize(in);
                    results.queue(message);
                    return message;

            }
            
        } catch (IOException e) {
        }finally{
            lockIn.unlock();
        }
        return null;
    }

    public void close(){
        try {
            this.socket.close();
        } catch (Exception ignore) {}
    }
}
