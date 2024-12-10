import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

// Classe para armazenar informações sobre os peers conectados
public class SocketAndStreams {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public SocketAndStreams(Socket socket, ObjectInputStream in, ObjectOutputStream out, String nome) {
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectInputStream getObjectInputStream() {
        return in;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return out;
    }

    public String getIpString() {
        return socket.getInetAddress().getHostAddress();
    }
}