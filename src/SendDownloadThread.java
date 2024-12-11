import java.util.List;
import java.nio.file.Path;
import java.util.ArrayList;
import java.io.File;


public class SendDownloadThread extends Thread {

    private DownloadTasksManager message;
    private List<FileBlockRequestMessage> requests;

    public SendDownloadThread(DownloadTasksManager message) {
        super();
        this.message = message;
        this.requests = new ArrayList<>();
    }

    @Override
    public void run() {
        while(!message.getBlockRequests().isEmpty()) {
            requests.add(message.getNextBlockRequest());
        }

        byte[] fileHash = message.getBlockRequests().get(0).getFileHash();
        String directoryPath = Path.of("").toAbsolutePath().resolve(message.getDownloadDirectory()).toString();
        File file = message.getFileByHashInDirectory(fileHash, message.getDownloadDirectory());

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            for (FileBlockRequestMessage request : requests) {
                byte[] buffer = new byte[request.getLength()];
                raf.seek(request.getOffset());
                raf.readFully(buffer);
                FileBlockAnswerMessage answer = new FileBlockAnswerMessage(request.getFileHash(), request.getOffset(), buffer);
                // Process the answer message as needed
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
