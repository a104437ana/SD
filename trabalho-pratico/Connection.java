import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Classe Connection responsável por enviar Messages da origem ao destino,
 * através de uma socket tcp
 * Utiliza Data Input/Output Stream para serializar e deserializar Messages
 */
public class Connection {
    private Socket socket;
    private DataInputStream in;   
    private DataOutputStream out;

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
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
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
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    /**
     * Método que permite enviar uma Message ao destinatário,
     * por tcp, a qual a socket se encontra conectada
     * @param m
     */
    public void send(Message m) {
            try {
                m.serialize(out);
                out.flush();
                
            } catch (IOException e) { }
    }

    /**
     * Método que permite receber uma Message do destinatário,
     * por tcp, a qual a socket se encontra conectada
     * @return
     */
    public Message receive() {
        try{
            String tipo = in.readUTF();
            Message message;
            switch (tipo) {
                case "Put":
                    message = Put.deserialize(in);
                    return message;
                case "ResPut":
                    message = ResPut.deserialize(in);
                    return message;  
                case "Get":
                    message = Get.deserialize(in);
                    return message;
                case "ResGet":
                    message = ResGet.deserialize(in);
                    return message; 
                case "MultiPut":
                    message = MultiPut.deserialize(in);                    
                    return message; 
                case "ResMultiPut":
                    message = ResMultiPut.deserialize(in);
                    return message;
                case "MultiGet":
                    message = MultiGet.deserialize(in);         
                    return message;
                case "ResMultiGet":
                    message = ResMultiGet.deserialize(in);
                    return message;
                case "GetWhen":
                    message = GetWhen.deserialize(in);         
                    return message;
                case "ResGetWhen":
                    message = ResGetWhen.deserialize(in);
                    return message;
                case "Exit":
                    message = Exit.deserialize(in);
                    return message;
                case "Login":
                    message = Login.deserialize(in);
                    return message;
                case "ResLogin":
                    message = ResLogin.deserialize(in);
                    return message;
                case "Register":
                    message = Register.deserialize(in);                  
                    return message;
                case "ResRegister":
                    message = ResRegister.deserialize(in);
                    return message;

            }
            
        } catch (IOException e) { }
        return null;
    }

    public void close(){
        try {
            this.socket.close();
        } catch (Exception ignore) {}
    }
}
