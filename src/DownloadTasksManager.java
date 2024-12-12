import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class DownloadTasksManager implements Serializable {
    private List<FileBlockRequestMessage> blockRequests;
    private List<FileBlockAnswerMessage> blockAnswers;
    private transient CountDownLatch latch; // Sincroniza o número de blocos restantes
    private String downloadDirectory; // Diretório de destino do ficheiro

    public DownloadTasksManager(byte[] fileHash, long fileSize, String downloadDirectory, FileSearchResult searchResults) {
        this.blockRequests = new ArrayList<>();
        this.downloadDirectory = downloadDirectory;

        // Divide o ficheiro em blocos
        long blockSize = 10240; // Tamanho padrão do bloco
        int numBlocks = (int) Math.ceil((double) fileSize / blockSize);
        this.latch = new CountDownLatch(numBlocks); // Inicializa com o número total de blocos

        for (long offset = 0; offset < fileSize; offset += blockSize) {
            int length = (int) Math.min(blockSize, fileSize - offset);
            blockRequests.add(new FileBlockRequestMessage(fileHash, offset, length));
        }
    }

    public synchronized List<FileBlockRequestMessage> getBlockRequests() {
        return blockRequests;
    }
    
    public String getDownloadDirectory() {
        return downloadDirectory;
    }

    public synchronized FileBlockRequestMessage getNextBlockRequest() {
        if (!blockRequests.isEmpty()) {
            return blockRequests.remove(0);
        }
        return null;
    }

    public File getFileByHashInDirectory(byte[] fileHash, String directoryPath) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        for (File file : files) {
            byte[] hash = null;
            try {
                hash = File_Hash.calculateHash(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (hash != null && Arrays.equals(hash, fileHash)) {
                return file;
            }
        }
        return null;
        
    }

    public synchronized void addBlockAnswer(FileBlockAnswerMessage message) {
        blockAnswers.add(message);
        latch.countDown(); // Indica que um bloco foi descarregado
    }

    public void writeFileToDisk() {
        
        new Thread(() -> {
            try {
                latch.await(); // Wait until all blocks have been received

                // Sort the block answers by their offset
                blockAnswers.sort(Comparator.comparingLong(FileBlockAnswerMessage::getBlockOffset));

                // Create the output file
                File outputFile = new File(downloadDirectory, "downloaded_file");

                // Write all blocks to the file
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    for (FileBlockAnswerMessage answer : blockAnswers) {
                        fos.write(answer.getBlockData()); // Write block data to file
                    }
                }

                System.out.println("File written successfully: " + outputFile.getAbsolutePath());
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }).start();
        
        
        /* new Thread(() -> {
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
        }).start(); */
    }
}

