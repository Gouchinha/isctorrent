import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class DownloadTasksManager {
    private List<FileBlockRequestMessage> blockRequests;
    private List<FileBlockAnswerMessage> blockAnswers;
    private Map<Long, byte[]> downloadedBlocks; // Armazena os blocos descarregados
    private CountDownLatch latch; // Sincroniza o número de blocos restantes
    private String downloadDirectory; // Diretório de destino do ficheiro
    private FileNode fileNode;

    public DownloadTasksManager(byte[] fileHash, long fileSize, String downloadDirectory, FileNode fileNode) {
        this.blockRequests = new ArrayList<>();
        this.downloadedBlocks = new ConcurrentHashMap<>();
        this.downloadDirectory = downloadDirectory;
        this.fileNode = fileNode;

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

    public void receiveBlock(long offset, byte[] data) {
        downloadedBlocks.put(offset, data);
        latch.countDown(); // Indica que um bloco foi descarregado
    }

    public void sendFileBlockRequests(FileSearchResult result) { // ISTO MANDA PARA TODOS OS NÓS TODOS OS BLOCOS (NÃO É ISTO)
        for (SocketAndStreams peer : fileNode.getConnectedPeers()) {
            if (peer.getIpString().equals(result.getNodeAddress()) && peer.getNodePort() == result.getNodePort()) {
                for (int i = 0; i < blockRequests.size(); i++) {
                    fileNode.sendMessage(peer, (Serializable) blockRequests.get(i));
                return;
            }
        }
            System.out.println("Peer não encontrado para download: " + result.getNodeAddress() + ":" + result.getNodePort());
        }
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

