import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;

public class DownloadTasksManager {
    private List<FileBlockRequestMessage> blockRequests;
    private List<FileBlockAnswerMessage> blockAnswers;
    private CountDownLatch latch; // Sincroniza o número de blocos restantes
    private String downloadDirectory; // Diretório de destino do ficheiro
    private FileNode fileNode;
    private FileSearchResult searchResults;

    public DownloadTasksManager(byte[] fileHash, long fileSize, String downloadDirectory, FileNode fileNode, FileSearchResult searchResults) {
        this.blockRequests = new ArrayList<>();
        this.downloadDirectory = downloadDirectory;
        this.fileNode = fileNode;
        this.searchResults = searchResults;

        // Divide o ficheiro em blocos
        long blockSize = 10240; // Tamanho padrão do bloco
        int numBlocks = (int) Math.ceil((double) fileSize / blockSize);
        this.latch = new CountDownLatch(numBlocks); // Inicializa com o número total de blocos

        for (long offset = 0; offset < fileSize; offset += blockSize) {
            int length = (int) Math.min(blockSize, fileSize - offset);
            blockRequests.add(new FileBlockRequestMessage(fileHash, offset, length));
        }
    }

    public List<FileBlockRequestMessage> getBlockRequests() {
        return blockRequests;
    }

    public synchronized FileBlockRequestMessage getNextBlockRequest() {
        if (!blockRequests.isEmpty()) {
            return blockRequests.remove(0);
        }
        return null;
    }

    public File getFileByHashInDirectory(byte[] fileHash) {
        
    }

    public synchronized void addBlockAnswer(FileBlockAnswerMessage message) {
        blockAnswers.add(message);
        latch.countDown(); // Indica que um bloco foi descarregado
    }

    public void sendFileBlockRequests(FileSearchResult result) { // ISTO MANDA PARA TODOS OS NÓS TODOS OS BLOCOS (NÃO É ISTO)
        for (SocketAndStreams peer : fileNode.getConnectedPeers()) {
            for (Map<String, Integer> r : result.getNodeswithFile()) {
                if (peer.getSocket().getInetAddress() == r.get(result) && peer.getSocket().getPort() == result.getNodePort()) {
                    fileNode.sendMessage(peer, (Serializable) this);
                }
            }
         }
        }

            System.out.println("Peer não encontrado para download: " + result.getNodeAddress() + ":" + result.getNodePort());
    }

    public void writeFileToDisk(String fileName) {
        
        
        
        
        new Thread(() -> {
            try {
                latch.await(); // Espera que todos os blocos sejam descarregados
                File file = new File(downloadDirectory, fileName);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    for (long offset = 0; offset < blockRequests.size() * 10240L; offset += 10240) {
                        byte[] data = downloadedBlocks.get(offset);
                        if (data != null) {
                            fos.write(data); // Escreve o bloco no disco
                        }
                    }
                }
                System.out.println("Ficheiro escrito com sucesso: " + file.getAbsolutePath());
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}

