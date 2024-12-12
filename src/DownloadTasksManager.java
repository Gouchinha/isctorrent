import java.io.*;
import java.util.*;

public class DownloadTasksManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<FileBlockRequestMessage> blockRequests;
    private List<FileBlockAnswerMessage> blockAnswers;
    private String downloadDirectory; // Destination directory for the file
    private int numBlocks; // Number of blocks to be downloaded
    private int remainingBlocks; // Number of remaining blocks to be received
    private int randomHash;

    public DownloadTasksManager(byte[] fileHash, long fileSize, String downloadDirectory, FileSearchResult searchResults) {
        this.blockRequests = new ArrayList<>();
        this.downloadDirectory = downloadDirectory;
        this.blockAnswers = new ArrayList<>();
        this.randomHash = new Random().nextInt(1000);
        this.randomHash = generateUniqueRandomHash();

        // Divide the file into blocks
        long blockSize = 10240; // Default block size
        this.numBlocks = (int) Math.ceil((double) fileSize / blockSize);
        this.remainingBlocks = numBlocks;

        for (long offset = 0; offset < fileSize; offset += blockSize) {
            int length = (int) Math.min(blockSize, fileSize - offset);
            blockRequests.add(new FileBlockRequestMessage(fileHash, offset, length, randomHash));
        }

        // Start the thread to write the file to disk
        writeFileToDisk();
    }

    private int generateUniqueRandomHash() {
        return new Random().nextInt(1000);
    }

    public synchronized List<FileBlockRequestMessage> getBlockRequests() {
        return blockRequests;
    }

    public synchronized int getDownloadRequestHash() {
        return this.randomHash;
    }

    public synchronized List<FileBlockAnswerMessage> getBlockAnswers() {
        return blockAnswers;
    }   
    
    public String getDownloadDirectory() {
        return downloadDirectory;
    }

    public synchronized FileBlockRequestMessage getNextBlockRequest() throws InterruptedException {
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
        System.out.println("Block answer received: " + message.getBlockOffset());
        blockAnswers.add(message);
        remainingBlocks--;
        System.out.println("Blocks remaining: " + remainingBlocks);

        if (remainingBlocks == 0) {
            notifyAll(); // Notify all waiting threads
        }
    }

    public void writeFileToDisk() {
        System.out.println("Creating thread to write file to disk...");
        new Thread(() -> {
            synchronized (this) {
                    try {
                        wait(3000); // Wait until all blocks have been received
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    }
                
                System.out.println("All blocks received. Writing file to disk...");

                // Sort the block answers by their offset
                blockAnswers.sort(Comparator.comparingLong(FileBlockAnswerMessage::getBlockOffset));

                for (FileBlockAnswerMessage answer : getBlockAnswers()) {
                    System.out.println("Block offset: " + answer.getBlockOffset());
                }
                // Create the output file
                /* File outputFile = new File(downloadDirectory, "downloaded_file");

                // Write all blocks to the file
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    for (FileBlockAnswerMessage answer : blockAnswers) {
                        fos.write(answer.getBlockData()); // Write block data to file
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } */
                System.out.println("File written successfully: ");
                //System.out.println("File written successfully: " + outputFile.getAbsolutePath());
            }
        }).start();
    }
}
