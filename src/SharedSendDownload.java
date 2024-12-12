import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class SharedSendDownload {

    private List<FileBlockRequestMessage> blockRequests;
    private int identifier;

    public SharedSendDownload(int identifier) {
        this.blockRequests = new ArrayList<>();
        this.identifier = identifier;
    }

    public synchronized int getIdentifier() {
        return identifier;
    }

    public synchronized FileBlockRequestMessage getNextBlockRequest() {
        FileBlockRequestMessage request = blockRequests.remove(0);
        
        return request;
    }

    public synchronized void addBlockRequest(FileBlockRequestMessage request) {
        blockRequests.add(request);
    }
}
