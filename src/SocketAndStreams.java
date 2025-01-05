import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.io.Serializable;

// Classe para armazenar informações sobre os peers conectados
public class SocketAndStreams implements Serializable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int peerPort;

    public SocketAndStreams(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    public void setPort(int port) {
        this.peerPort = port;
    }

    public Socket getSocket() {
        return socket;
    }

    public int getNodePort() {
        return peerPort;
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