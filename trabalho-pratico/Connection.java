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
    public void send(Message m) {
    }

    /**
     * Método que permite receber uma Message do destinatário,
     * por tcp, a qual a socket se encontra conectada
     * @return
     */
    public Message receive() {
        return null;
    }

    public void close(){
        try {
            this.socket.close();
        } catch (Exception ignore) {}
    }
}
