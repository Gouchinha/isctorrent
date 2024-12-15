import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.io.Serializable;

public class SharedResultList implements Serializable {

    private List<FileSearchResult> finalResults;
    private transient CountDownLatch latch;
    private SearchGUI gui;
    private final ReentrantLock lock = new ReentrantLock();

    public SharedResultList(int numberOfPeersWithFile, SearchGUI gui) {
        this.finalResults = new CopyOnWriteArrayList<>();
        this.latch = new CountDownLatch(numberOfPeersWithFile);
        this.gui = gui;
    }

    public void add(List<FileSearchResult> result) {
        lock.lock();
        System.out.println("Adding results to shared list - " + result.get(0).getNodePort());
        try {
            if (!finalResults.isEmpty()) {
                for (FileSearchResult r : result) {
                    processExistingResult(r);
                }
                latch.countDown(); // Decrement the latch count
            } else {
                finalResults.addAll(result);
                latch.countDown(); // Decrement the latch count
            }
        } finally {
            lock.unlock();
        }
        synchronized (this) {
            notifyAll();
        }
    }

    private void processExistingResult(FileSearchResult r) {
        for (FileSearchResult existing : finalResults) {
            System.out.println("Result: " + r.getFileHash() + " - " + r.getFileName() + " - " + r.getNodePort());
            System.out.println("Comparing to" + existing.getFileHash() + " - " + existing.getFileName() + " - " + existing.getNodePort());
            if (r.getFileHash().equals(existing.getFileHash()) || r.getFileName().equals(existing.getFileName())) {
                System.out.println("Duplicate result found: " + existing.getFileName());
                existing.addNodeWithFile(new String[] { r.getNodeAddress(), String.valueOf(r.getNodePort()) });
                return;
            }
        }
        finalResults.add(r);
        System.out.println("Result added: " + r.getFileName());
    }

    public synchronized void waitAndGetResults() throws InterruptedException {
        System.out.println("Waiting for final results...");
        try {
            latch.await(); // Wait until all peers have responded
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted: " + e.getMessage());
        }
        System.out.println("Final Results arrived - " + finalResults);
        gui.updateSearchResults(finalResults);
    }
}
