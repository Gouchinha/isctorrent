import java.util.List;
import java.util.ArrayList;

public class SharedSendDownload {

    private List<FileBlockRequestMessage> blockRequests;
    private int identifier;

    public SharedSendDownload(int identifier) {
        this.blockRequests = new ArrayList<>();
        this.identifier = identifier;
    }

    public synchronized List<FileBlockRequestMessage> getBlockRequests() {
        return blockRequests;
    }

    public synchronized int getIdentifier() {
        return identifier;
    }

    public synchronized FileBlockRequestMessage getNextBlockRequest() {
        System.out.println("Getting next block request");   
        while (getBlockRequests().isEmpty()) {
            try {
            wait();
            } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            }
        }
        FileBlockRequestMessage request = blockRequests.remove(0);
        
        return request;
    }

    public synchronized void addBlockRequest(FileBlockRequestMessage request) {
        System.out.println("Adding block request: " + request.getOffset());
        blockRequests.add(request);
        notifyAll();   
    }
}
