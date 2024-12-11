import java.util.List;
import java.util.ArrayList;


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

        File file = message.getFileByHash(requests.get(0).getFileHash());

        for (FileBlockRequestMessage request : requests) {
            System.out.println("Creating FileBlockAnswer to resquest: " + request);
            FileBlockAnswerMessage answer = new FileBlockAnswerMessage(request.getFileHash(), request.getOffset(), request.getLength(), new byte[request.getLength()]);
        }
    }
}
