import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class FileNode {
    private int port;
    private ServerSocket serverSocket;
    private List<SocketAndStreams> connectedPeers;
    private FileLoader fileLoader;
    private SearchGUI gui;
    private DownloadTasksManager downloadTasksManager;
    private SharedResultList sharedResultList;

    public FileNode(int port, String pastaDownload) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.connectedPeers = new ArrayList<>();
        this.fileLoader = new FileLoader(pastaDownload);
        this.sharedResultList = null;

        System.out.println("Nó inicializado na porta: " + port);

        SwingUtilities.invokeLater(() -> {
            gui = new SearchGUI(this);
            gui.setVisible(true);
        });
        // Inicia um thread para aceitar conexões
        Thread acceptConnectionsThread = new Thread(this::acceptConnections);
        acceptConnectionsThread.setName("AcceptConnectionsThread");
        acceptConnectionsThread.start();

        // Inicia um thread para receber List<FileSearchResult> e juntar numa nova Lista e atualizar a GUI
        
    }

    private void acceptConnections() {
        System.out.println("Aguardando conexões...");
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nova conexão de: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                SocketAndStreams peer = new SocketAndStreams(clientSocket, in, out, clientSocket.getInetAddress().getHostName());
                connectedPeers.add(peer);

                // Inicia um thread para tratar mensagens do peer conectado
                Thread handlePeerThread = new Thread(() -> handlePeer(peer));
                handlePeerThread.setName("HandlePeerThread-" + peer.getIpString());
                handlePeerThread.start();
            }
        } catch (IOException e) {
            System.err.println("Erro ao aceitar conexão: " + e.getMessage());
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
                System.out.println("Não é permitido conectar ao próprio nó.");
                return;
            }

            Socket peerSocket = new Socket(ipAddress, peerPort);
            ObjectOutputStream out = new ObjectOutputStream(peerSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(peerSocket.getInputStream());

            SocketAndStreams peer = new SocketAndStreams(peerSocket, in, out, ipAddress);
            connectedPeers.add(peer);

            System.out.println("Conexão estabelecida com " + ipAddress + ":" + peerPort);
            Thread handlePeerThread = new Thread(() -> handlePeer(peer));
            handlePeerThread.setName("HandlePeerThread-" + peer.getIpString());
            handlePeerThread.start();

        } catch (IOException e) {
            System.err.println("Erro ao conectar com " + ipAddress + ":" + peerPort + ": " + e.getMessage());
        }
    }

    private void handlePeer(SocketAndStreams peer) {
        try {
            while (true) {
                Object message = peer.getObjectInputStream().readObject();
                if (message instanceof WordSearchMessage) {
                    handleWordSearchRequest((WordSearchMessage) message, peer);
                } else if (message instanceof List<?>) {
                    if (message instanceof List<?>) {
                        List<?> list = (List<?>) message;
                        if (!list.isEmpty() && list.get(0) instanceof FileSearchResult) {
                            handleFileSearchResult((List<FileSearchResult>) list);
                        } else {
                            System.out.println("Não há resultados para a busca ");
                            gui.updateSearchResults((List<FileSearchResult>) list); 
                            JOptionPane.showMessageDialog(gui, "Nenhum resultado encontrado!");                       
                        }
                    } else {
                        System.out.println("Mensagem inválida recebida de " + peer.getIpString() + ": " + message);
                    }
                } else {
                    System.out.println("Mensagem inválida recebida de " + peer.getIpString() + ": " + message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Conexão encerrada com " + peer.getIpString());
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
        sendMessage(peer, (Serializable) results);
    }

    private void handleFileSearchResult(List<FileSearchResult> results) {
        System.out.println("Resultados da busca recebidos: " + results);
        sharedResultList.add(results);
        //gui.updateSearchResults(results);
    }

    public void sendWordSearchRequest(String searchWord) {
        WordSearchMessage message = new WordSearchMessage(searchWord);
        sharedResultList = new SharedResultList(connectedPeers.size(), gui);
        
        Thread handlePeerThread = new Thread(() -> {
            try {
                sharedResultList.waitAndGetResults();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted: " + e.getMessage());
            }
        });
        handlePeerThread.setName("waitForResults");
        handlePeerThread.start();

        for (SocketAndStreams peer : connectedPeers) {
            sendMessage(peer, message);
        }
    }

    private void sendMessage(SocketAndStreams peer, Serializable message) {
        try {
            peer.getObjectOutputStream().writeObject(message);
            peer.getObjectOutputStream().flush();
            System.out.println("Mensagem enviada para " + peer.getIpString() + ": " + message);
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem para " + peer.getIpString() + ": " + e.getMessage());
        }
    }

}
