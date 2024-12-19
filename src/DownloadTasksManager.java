import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import javax.swing.*;

public class DownloadTasksManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<FileBlockRequestMessage> blockRequests;
    private List<FileBlockAnswerMessage> blockAnswers;
    private String downloadDirectory; // Destination directory for the file
    private int numBlocks; // Number of blocks to be downloaded
    private int randomHash;
    private transient CountDownLatch latch;
    private FileSearchResult searchResult;

    public DownloadTasksManager(int fileHash, long fileSize, String downloadDirectory, FileSearchResult searchResults) {
        this.blockRequests = new ArrayList<>();
        this.downloadDirectory = downloadDirectory;
        this.blockAnswers = new ArrayList<>();
        this.randomHash = new Random().nextInt(100000);
        this.randomHash = generateUniqueRandomHash();
        this.searchResult = searchResults;

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

    public synchronized int getRemainingBlocks() {
        return blockRequests.size();
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
        System.out.println("Block answer received: " + message.getBlockOffset() + " " + message.getFileHash());
        blockAnswers.add(message);
        latch.countDown();
        System.out.println("Blocks remaining: " + latch.getCount());
        notifyAll();
    }

    public void writeFileToDisk() {
        long startTime = System.currentTimeMillis();
        System.out.println("Creating thread to write file to disk...");
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

        System.out.println("All blocks received. Writing file to disk..." + blockAnswers.size() + " blocks");

        // Sort the block answers by their offset
        blockAnswers.sort(Comparator.comparingLong(FileBlockAnswerMessage::getBlockOffset));
        for (FileBlockAnswerMessage answer : blockAnswers) {
            int i = blockAnswers.indexOf(answer);
            System.out.println("Block offset: " + answer.getBlockOffset() + " - Block nº = " + i + " - Hash: " + answer.getFileHash());
        }

        // Give a name pop up
        /* JFrame frame = new JFrame("File Name Input");
        String fileName = JOptionPane.showInputDialog(frame, "Enter the name of the file to save:");
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "defaultFileName.mp3"; // Default file name if user cancels or enters an empty name
        } */

        File outputFile = new File(downloadDirectory, searchResult.getFileName());  

        // Write all blocks to the file
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            for (FileBlockAnswerMessage answer : blockAnswers) {
                fos.write(answer.getBlockData()); // Write block data to file
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("File written successfully: " + outputFile.getAbsolutePath());

        long endTime = System.currentTimeMillis();

        // Count the number of blocks per node
        Map<String, Integer> nodeBlockCount = new HashMap<>();
        for (FileBlockAnswerMessage answer : blockAnswers) {
            // Assuming FileBlockAnswerMessage includes IP and port attributes
            String nodeKey = String.format("endereco=%s, porto=%d", answer.getIpString(), answer.getPort());
            nodeBlockCount.put(nodeKey, nodeBlockCount.getOrDefault(nodeKey, 0) + 1);
        }

        // Create a custom panel for the summary
        JFrame summaryFrame = new JFrame("Transfer Summary");
        summaryFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        summaryFrame.setSize(400, 300);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Descarga completa."));
        for (Map.Entry<String, Integer> entry : nodeBlockCount.entrySet()) {
            panel.add(new JLabel(String.format("Fornecedor [%s]: %d blocos", entry.getKey(), entry.getValue())));
        }        
        long elapsedTime = (endTime - startTime) / 1000; // Convert milliseconds to seconds
        panel.add(new JLabel("Tempo decorrido: " + elapsedTime + " segundos"));

        summaryFrame.add(panel);
        summaryFrame.setVisible(true);
    }
}
