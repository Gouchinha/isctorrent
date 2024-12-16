import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class DownloadTasksManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<FileBlockRequestMessage> blockRequests;
    private List<FileBlockAnswerMessage> blockAnswers;
    private String downloadDirectory; // Destination directory for the file
    private int numBlocks; // Number of blocks to be downloaded
    private int randomHash;
    private transient CountDownLatch latch;

    public DownloadTasksManager(int fileHash, long fileSize, String downloadDirectory, FileSearchResult searchResults) {
        this.blockRequests = new ArrayList<>();
        this.downloadDirectory = downloadDirectory;
        this.blockAnswers = new ArrayList<>();
        this.randomHash = new Random().nextInt(100000);
        this.randomHash = generateUniqueRandomHash();
        


        // Divide the file into blocks
        long blockSize = 10240; // Default block size
        this.numBlocks = (int) Math.ceil((double) fileSize / blockSize);
        this.latch = new CountDownLatch(numBlocks); 

        for (long offset = 0; offset < fileSize; offset += blockSize) {
            int length = (int) Math.min(blockSize, fileSize - offset);
            boolean isLastBlock = offset + length >= fileSize;
            blockRequests.add(new FileBlockRequestMessage(fileHash, offset, length, randomHash, isLastBlock));
        }

        // Start the thread to write the file to disk
        new Thread() {
            public void run() {
                writeFileToDisk();
            }
        }.start();
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
            return blockRequests.remove(0);
    }

    public synchronized void addBlockAnswer(FileBlockAnswerMessage message) {
        System.out.println("Block answer received: " + message.getBlockOffset());
        blockAnswers.add(message);
        latch.countDown();
        System.out.println("Blocks remaining: " + latch.getCount());
        notifyAll();
    }

    public void writeFileToDisk() {
        System.out.println("Creating thread to write file to disk...");
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
                
                System.out.println("All blocks received. Writing file to disk...");

                // Sort the block answers by their offset
                blockAnswers.sort(Comparator.comparingLong(FileBlockAnswerMessage::getBlockOffset));
                for (FileBlockAnswerMessage answer : blockAnswers) {
                    System.out.println("Block offset: " + answer.getFileHash() + answer.getBlockOffset());
                }

                // Create the output file
                 File outputFile = new File(downloadDirectory, "downloaded_file");

                // Write all blocks to the file
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    for (FileBlockAnswerMessage answer : blockAnswers) {
                        fos.write(answer.getBlockData()); // Write block data to file
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("File written successfully: ");
                //System.out.println("File written successfully: " + outputFile.getAbsolutePath());
    }
}
