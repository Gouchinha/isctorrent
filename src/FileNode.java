import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.SwingUtilities;

public class FileNode {
    private int port;
    private ServerSocket serverSocket;
    private List<SocketAndStreams> connectedPeers;
    private FileLoader fileLoader;
    private SearchGUI gui;
  

    // Classe para armazenar informa��es sobre os peers conectados
    public static class SocketAndStreams {
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private String nome;

        public SocketAndStreams(Socket socket, ObjectInputStream in, ObjectOutputStream out, String nome) {
            this.socket = socket;
            this.in = in;
            this.out = out;
            this.nome = nome;
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

        public String getNome() {
            return nome;
        }
    }

    public FileNode(int port, String pastaDownload) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.connectedPeers = new ArrayList<>();
        this.fileLoader = new FileLoader(pastaDownload);
        

        System.out.println("N� inicializado na porta: " + port);

        SwingUtilities.invokeLater(() -> {
            gui = new SearchGUI(this);
            gui.setVisible(true);
        });
        // Inicia um thread para aceitar conex�es
        new Thread(this::acceptConnections).start();
    }

    private void acceptConnections() {
        System.out.println("Aguardando conex�es...");
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nova conex�o de: " + clientSocket.getInetAddress());

                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                SocketAndStreams peer = new SocketAndStreams(clientSocket, in, out, clientSocket.getInetAddress().getHostName());
                connectedPeers.add(peer);

                // Inicia um thread para tratar mensagens do peer conectado
                new Thread(() -> handlePeer(peer)).start();
            }
        } catch (IOException e) {
            System.err.println("Erro ao aceitar conex�o: " + e.getMessage());
        }
    }

    private void handlePeer(SocketAndStreams peer) {
        try {
            while (true) {
                Object message = peer.getObjectInputStream().readObject();
                if (message instanceof WordSearchMessage) {
                    handleWordSearchRequest((WordSearchMessage) message, peer);
                } else if (message instanceof List<?>) {
                    handleFileSearchResult((List<FileSearchResult>) message);
                } else {
                    System.out.println("Mensagem inv�lida recebida de " + peer.getNome() + ": " + message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Conex�o encerrada com " + peer.getNome());
            connectedPeers.remove(peer);
            try {
                peer.getSocket().close();
            } catch (IOException ex) {
                System.err.println("Erro ao fechar socket: " + ex.getMessage());
            }
        }
    }

    private void handleWordSearchRequest(WordSearchMessage message, SocketAndStreams peer) throws IOException {
        List<FileSearchResult> results = new ArrayList<>();
        for (File_Hash file_Hash : fileLoader.getFiles()) {
            if (file_Hash.getName().contains(message.getSearchWord())) {
                results.add(new FileSearchResult(
                        message,
                        file_Hash.getName(),
                        file_Hash.getFile().length(),
                        file_Hash.getHash(),
                        peer.getSocket().getInetAddress().getHostAddress(),
                        port));
            }
        }
        sendFileSearchResults(results, peer);
    }

    private void handleFileSearchResult(List<FileSearchResult> results) {
        System.out.println("Resultados da busca recebidos: " + results);
    }

    public void sendWordSearchRequest(String searchWord) {
        WordSearchMessage message = new WordSearchMessage(searchWord);
        for (SocketAndStreams peer : connectedPeers) {
            sendMessage(peer, message);
        }
    }

    private void sendFileSearchResults(List<FileSearchResult> results, SocketAndStreams peer) {
        sendMessage(peer, (Serializable) results);
    }

    private void sendMessage(SocketAndStreams peer, Serializable message) {
        try {
            peer.getObjectOutputStream().writeObject(message);
            peer.getObjectOutputStream().flush();
            System.out.println("Mensagem enviada para " + peer.getNome() + ": " + message);
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem para " + peer.getNome() + ": " + e.getMessage());
        }
    }


    public void connectToPeer(String ipAddress, int peerPort) {
        try {
            if (peerPort < 1 || peerPort > 65535) {
                System.out.println("A porta deve estar entre 1 e 65535.");
                return;
            }

            if ((ipAddress.equals(InetAddress.getLocalHost().getHostAddress())
                    || ipAddress.equals("localhost") || ipAddress.equals("127.0.0.1"))
                    && peerPort == this.port) {
                System.out.println("Não � permitido conectar ao pr�prio n�.");
                return;
            }

            Socket peerSocket = new Socket(ipAddress, peerPort);
            ObjectOutputStream out = new ObjectOutputStream(peerSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(peerSocket.getInputStream());

            SocketAndStreams peer = new SocketAndStreams(peerSocket, in, out, ipAddress);
            connectedPeers.add(peer);

            System.out.println("Conex�o estabelecida com " + ipAddress + ":" + peerPort);
            new Thread(() -> handlePeer(peer)).start();

        } catch (IOException e) {
            System.err.println("Erro ao conectar com " + ipAddress + ":" + peerPort + ": " + e.getMessage());
        }
    }
}
