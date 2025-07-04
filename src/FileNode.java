import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.util.concurrent.*;

public class FileNode implements Serializable {
    private int port;
    private ServerSocket serverSocket;
    private List<SocketAndStreams> connectedPeers;
    private FileLoader fileLoader;
    private SearchGUI gui;
    private SharedResultList sharedResultList;
    private ExecutorService threadPool;
    private DownloadTasksManager downloadTasksManager;
    private List<ThreadPoolQueue> threadPoolsQueues = new ArrayList<>();

    public FileNode(int port, String pastaDownload) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.connectedPeers = new ArrayList<>();
        this.fileLoader = new FileLoader(pastaDownload);
        this.sharedResultList = null;
        this.threadPool = Executors.newFixedThreadPool(5);
        System.out.println("Nó inicializado na porta: " + port);

        SwingUtilities.invokeLater(() -> {
            gui = new SearchGUI(this);
            gui.setVisible(true);
        });
        // Inicia um thread para aceitar conexões
        Thread acceptConnectionsThread = new Thread(this::acceptConnections);
        acceptConnectionsThread.setName("AcceptConnectionsThread");
        acceptConnectionsThread.start();

        // Inicia um thread para receber List<FileSearchResult> e juntar numa nova Lista
        // e atualizar a GUI

    }

    public int getPort() {
        return port;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public List<SocketAndStreams> getConnectedPeers() {
        return connectedPeers;
    }

    public FileLoader getFileLoader() {
        return fileLoader;
    }

    public SearchGUI getGui() {
        return gui;
    }

    public SharedResultList getSharedResultList() {
        return sharedResultList;
    }

    private void acceptConnections() {
        System.out.println("Aguardando conexões...");
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nova conexão de: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                SocketAndStreams peer = new SocketAndStreams(clientSocket, in, out);
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
        System.out.println("Conectando a " + ipAddress + ":" + peerPort);
        if (ipAddress.equals("localhost")) {
            ipAddress = "127.0.0.1";
        }
        try {
            if (peerPort < 1 || peerPort > 65535) {
                System.out.println("A porta deve estar entre 1 e 65535.");
                JOptionPane.showMessageDialog(gui, "A porta deve estar entre 1 e 65535.");
                return;
            }

            for (SocketAndStreams peer : connectedPeers) {
                System.out.println("Peer: " + peer.getIpString() + ":" + peer.getNodePort());
                if ((peer.getIpString().equals(ipAddress)) && peer.getNodePort() == peerPort) {
                    System.out.println("Já conectado a " + ipAddress + ":" + peerPort);
                    JOptionPane.showMessageDialog(gui, "Já conectado a " + ipAddress + ":" + peerPort);
                    return;
                }
            }

            if (isSelfConnection(ipAddress, peerPort)) {
                System.out.println("Não é permitido conectar ao próprio nó.");
                JOptionPane.showMessageDialog(gui, "Não é permitido conectar ao próprio nó.");
                return;
            }

            Socket peerSocket = new Socket(ipAddress, peerPort);
            ObjectOutputStream out = new ObjectOutputStream(peerSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(peerSocket.getInputStream());
            SocketAndStreams peer = new SocketAndStreams(peerSocket, in, out);
            peer.setPort(peerPort);
            sendMessage(peer, port);
            connectedPeers.add(peer);

            System.out.println("Conexão estabelecida com " + ipAddress + ":" + peerPort);
            Thread handlePeerThread = new Thread(() -> handlePeer(peer));
            handlePeerThread.setName("HandlePeerThread-" + peer.getIpString());
            handlePeerThread.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(gui,
                    "Erro ao conectar com ou não existe" + ipAddress + ":" + peerPort + ": " + e.getMessage());
            System.err.println("Erro ao conectar com " + ipAddress + ":" + peerPort + ": " + e.getMessage());
        }
    }

    private boolean isSelfConnection(String ipAddress, int peerPort) {
        try {
            return (ipAddress.equals(InetAddress.getLocalHost().getHostAddress())
                    || ipAddress.equals("localhost") || ipAddress.equals("127.0.0.1"))
                    && peerPort == this.port;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void handlePeer(SocketAndStreams peer) {
        try {
            while (true) {
                Object message = peer.getObjectInputStream().readObject();
                if (message instanceof Integer) {
                    System.out.println("Porta " + message + " recebida de " + peer.getIpString());
                    peer.setPort((Integer) message); // Atualiza a porta do peer 
                }
                handleMessage(message, peer);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Conexão encerrada com " + peer.getIpString() + ":" + peer.getNodePort() + ": " + e.getMessage());
            connectedPeers.remove(peer);
            try {
                peer.getSocket().close();
            } catch (IOException ex) {
                System.err.println("Erro ao fechar socket: " + ex.getMessage());
            }
        }
    }

    private void handleMessage(Object message, SocketAndStreams peer) {
        if (message instanceof WordSearchMessage) {
            try {
                handleWordSearchRequest((WordSearchMessage) message, peer);
            } catch (IOException e) {
                System.err.println("Erro ao processar WordSearchMessage: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (message instanceof List<?>) {
            handleListMessage((List<?>) message);
        } else if (message instanceof FileBlockRequestMessage) {
            handleFileBlockRequest((FileBlockRequestMessage) message, peer);
        } else if (message instanceof FileBlockAnswerMessage) {
            handleFileBlockAnswer((FileBlockAnswerMessage) message);
        } else {
            System.out.println("Mensagem inválida recebida de " + peer.getIpString() + ": " + message);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleListMessage(List<?> list) {
        new Thread() {
            public void run() {
                handleFileSearchResult((List<FileSearchResult>) list);
            }
        }.start();
    }

    private void handleFileBlockRequest(FileBlockRequestMessage request, SocketAndStreams peer) {
        System.out.println("Handling FileBlockRequestMessage from " + peer.getIpString() + ":" + peer.getNodePort());
        System.out.println("Pedido de bloco recebido: " + request.getOffset());
        String threadName = "SendDownloadThread-" + request.getDownloadIdentifier();
        ThreadPoolQueue existingThreadPoolQueue = findExistingSharedSendDownload(request);

        if (existingThreadPoolQueue != null) {
            // Adicionar o novo pedido à queue da thread já existente
            System.out.println("Adicionando pedido à queue existente");
            existingThreadPoolQueue.addBlockRequest(request);
            return;

        } else {
            // Caso contrário, criar um novo SharedSendDownload
            ThreadPoolQueue newThreadPoolQueue = new ThreadPoolQueue(request.getDownloadIdentifier());
            threadPoolsQueues.add(newThreadPoolQueue);

            // Criar uma nova thread para processar os pedidos dessa queue
            Thread sendDownloadThread = new Thread(() -> {
                try {
                    while (true) {
                        FileBlockRequestMessage blockRequest = newThreadPoolQueue.takeBlockRequest();
                        if (blockRequest == null) { // Sinal de término
                            break;
                        }
                        // Processar o pedido
                        processRequest(blockRequest, fileLoader.getDirectoryPath(), peer);
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Tratar interrupções
                } finally {
                    // Remover o SharedSendDownload da lista ao terminar
                    synchronized (newThreadPoolQueue) {
                        threadPoolsQueues.remove(newThreadPoolQueue);
                    }
                    System.out.println(threadName + " terminada");
                }
            });
            sendDownloadThread.setName(threadName);
            threadPool.submit(sendDownloadThread);
            newThreadPoolQueue.addBlockRequest(request); // Adicionar o primeiro bloco à queue
        }
    }

    private void handleFileBlockAnswer(FileBlockAnswerMessage answer) {
        System.out.println("Block answer received: " + answer.getBlockOffset() + " from " + answer.getFileHash());
        downloadTasksManager.addBlockAnswer(answer);
    }

    private ThreadPoolQueue findExistingSharedSendDownload(FileBlockRequestMessage request) {
        for (ThreadPoolQueue sharedSendDownload : threadPoolsQueues) {
            if (sharedSendDownload.getIdentifier() == request.getDownloadIdentifier()) {
                System.out.println("SharedSendDownload já existe para " + request.getDownloadIdentifier());
                return sharedSendDownload;
            }
        }
        return null;
    }

    public void processRequest(FileBlockRequestMessage request, String directory, SocketAndStreams peer)
            throws InterruptedException {
        System.out.println("Criando resposta a pedido: " + request.getOffset());
        int fileHash = request.getFileHash();
        long offset = request.getOffset();
        int length = request.getLength();
        File file = getFileByHashInDirectory(fileHash, directory);
        byte[] data = new byte[length];
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(offset);
            raf.read(data, 0, length);
            raf.close();

            // Create and send FileBlockAnswerMessage
            FileBlockAnswerMessage answerMessage = new FileBlockAnswerMessage(fileHash, offset, data,
                    peer.getIpString(), port);
            System.out.println("Block answer created: " + answerMessage.getBlockOffset());

            sendBlockAnswer(answerMessage, peer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getFileByHashInDirectory(int fileHash, String directoryPath) throws InterruptedException {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        for (File file : files) {
            try {
                File_Hash file_Hash = new File_Hash(file);
                if (file_Hash.getHash() != 0 && file_Hash.getHash() == fileHash) {
                    return file;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void sendBlockAnswer(FileBlockAnswerMessage answerMessage, SocketAndStreams peer) {
        sendMessage(peer, answerMessage);
        System.out.println("Block answer sent: " + answerMessage.getBlockOffset());
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
                        peer.getSocket().getLocalAddress().getHostAddress(),
                        port));
            }
        }
        sendMessage(peer, (Serializable) results);
    }

    private void handleFileSearchResult(List<FileSearchResult> results) {
        System.out.println("Search results received: " + results);
        sharedResultList.add(results);
    }

    public void startDownload(FileSearchResult result) {
        System.out.println("Starting download of " + result.getFileName());
        downloadTasksManager = new DownloadTasksManager(result.getFileHash(), result.getFileSize(),
                fileLoader, result);

        for (SocketAndStreams peer : connectedPeers) {
            System.out.println("Peer ID " + peer.getIpString() + ":" + peer.getNodePort());
            for (String[] r : result.getNodesWithFile().getList()) {
                System.out.println("Result ID " + r[0] + ":" + r[1]);
                if (isPeerMatchingResult(peer, r)) {
                    startDownloadThread(peer);
                }
            }
        }

    }

    private boolean isPeerMatchingResult(SocketAndStreams peer, String[] r) {
        return peer.getIpString().equals(r[0]) && peer.getNodePort() == Integer.parseInt(r[1]);
    }

    private void startDownloadThread(SocketAndStreams peer) {
        Thread downloadThread = new Thread() {
            public void run() {
                try {
                    System.out.println("Thread " + getName() + " started");
                    while (true) {
                        if (!downloadTasksManager.getBlockRequests().isEmpty()) {
                            FileBlockRequestMessage request = downloadTasksManager.getNextBlockRequest();
                            System.out.println("Block request sent to " + peer.getNodePort() + ": " + request.getOffset() + " " + request.getFileHash());
                            sendMessage(peer, request);
                            synchronized (downloadTasksManager) {
                                System.out.println(getName() + " sleeping");
                                downloadTasksManager.wait();
                                System.out.println(getName() + " awaked");
                            }
                        } else {
                            System.out.println(getName() + "terminada");
                            break;
                        }

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        downloadThread.setName("DownloadThread-" + peer.getNodePort());
        downloadThread.start();
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

    protected void sendMessage(SocketAndStreams peer, Serializable message) {
        try {
            peer.getObjectOutputStream().writeObject(message);
            peer.getObjectOutputStream().flush();
            System.out.println("Mensagem enviada para " + peer.getIpString() + ":" + peer.getNodePort() + ": " + message);
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem para " + peer.getIpString() + ":" + peer.getNodePort() + ": "
                    + e.getMessage());
            e.printStackTrace();
        }
    }

}
