import java.util.List;
//import java.nio.file.Path;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;


public class SendDownloadThread extends Thread {

    private FileBlockRequestMessage message;
    private String directoryPath;
    private SharedSendDownload sharedSendDownload;

    public SendDownloadThread(FileBlockRequestMessage message, String directoryPath, SharedSendDownload sharedSendDownload) {
        System.out.println("SendDownloadThread initiated");
        super();
        this.message = message;
        this.directoryPath = directoryPath;
        this.sharedSendDownload = sharedSendDownload;
    }

    @Override
    public void run() {
         System.out.println("SendDownloadThread running");
        while(true) {
            FileBlockRequestMessage request = sharedSendDownload.getNextBlockRequest();
            if (request == null) {
                break;
            }
            processRequest(request);
        }
        
        
        
        
        
        /* // Get all block requests
        List<FileBlockRequestMessage> requests = message.getBlockRequests();

        if (requests.isEmpty()) {
            System.err.println("No requests to process.");
            return; // No requests to process
        }
        System.out.println("Requests to process: " + requests.size());
        // Get the file hash from the first request
        byte[] fileHash = requests.get(0).getFileHash();
        // Get the file to send
        //String directoryPath = Path.of("").toAbsolutePath().resolve(message.getDownloadDirectory()).toString();
        File file = message.getFileByHashInDirectory(fileHash, directoryPath);

        if (file == null || !file.exists()) {
            System.err.println("File not found.");
            return;
        }

        System.out.println("File found: " + file.getName());

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            List<FileBlockAnswerMessage> answers = new ArrayList<>();

            for (FileBlockRequestMessage request : requests) {
            long offset = request.getOffset();
            int length = request.getLength();

            // Read the requested block
            byte[] blockData = new byte[length];
            raf.seek(offset);
            int bytesRead = raf.read(blockData, 0, length);

            if (bytesRead == length) {
                // Create a FileBlockAnswerMessage with the block data
                FileBlockAnswerMessage answer = new FileBlockAnswerMessage(fileHash, offset, blockData);
                answers.add(answer);
            }
            }

            System.out.println("Answers to send: " + answers.size());

            // Send the answers
            for (FileBlockAnswerMessage answer : answers) {
                System.out.println("Sending answer: " + answer.getBlockOffset());
                message.addBlockAnswer(answer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    } */
}
