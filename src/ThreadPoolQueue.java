import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ThreadPoolQueue {

    private final long identifier;
    private final BlockingQueue<FileBlockRequestMessage> queue = new LinkedBlockingQueue<>();
    // private volatile boolean active = true; // Flag para controlar o estado ativo

    public ThreadPoolQueue(long identifier) {
        this.identifier = identifier;
    }

    public long getIdentifier() {
        return identifier;
    }

    public synchronized void addBlockRequest(FileBlockRequestMessage request) {
        System.out.println("Added block request to queue");
        queue.add(request);
        notifyAll();
    }

    public synchronized FileBlockRequestMessage takeBlockRequest() {
        if (queue.isEmpty()) {
            try {
                System.out.println("Waiting for block request in queue to take");
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Took block request from queue");
        return queue.poll();
    }

    
    
}
