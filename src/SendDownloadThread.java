import java.util.List;
//import java.nio.file.Path;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


public class SendDownloadThread extends Thread {

    private DownloadTasksManager message;

    public SendDownloadThread(DownloadTasksManager message) {
        super();
        this.message = message;
    }

    @Override
    public void run() {
        // Get all block requests
        List<FileBlockRequestMessage> requests = message.getBlockRequests();

        if (requests.isEmpty()) {
            return; // No requests to process
        }

        // Get the file hash from the first request
        byte[] fileHash = requests.get(0).getFileHash();
        // Get the file to send
        //String directoryPath = Path.of("").toAbsolutePath().resolve(message.getDownloadDirectory()).toString();
        File file = message.getFileByHashInDirectory(fileHash, message.getDownloadDirectory());

        if (file == null || !file.exists()) {
            System.err.println("File not found.");
            return;
        }

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

            // Send the answers
            for (FileBlockAnswerMessage answer : answers) {
                message.addBlockAnswer(answer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
