import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class SharedSendDownload {

    private final long identifier;
    private final BlockingQueue<FileBlockRequestMessage> queue = new LinkedBlockingQueue<>();
    private volatile boolean active = true; // Flag para controlar o estado ativo

    public SharedSendDownload(long identifier) {
        this.identifier = identifier;
    }

    public long getIdentifier() {
        return identifier;
    }

    public void addBlockRequest(FileBlockRequestMessage request) {
        System.out.println("Added block request to queue");
        queue.add(request);
    }

    public FileBlockRequestMessage takeBlockRequest() {
        System.out.println("Took block request from queue");
        return queue.poll();
    }

    
    
}
