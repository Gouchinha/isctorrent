import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.swing.DefaultListModel;
import javax.swing.AbstractListModel;
import javax.swing.JList;
import java.net.InetAddress;
import java.util.Map;
import java.util.HashMap;

public class SharedResultList {

    private List<FileSearchResult> results;
    private CountDownLatch latch;
    private SearchGUI gui;

    public SharedResultList(int numberOfPeers, SearchGUI gui) {
        this.results = new ArrayList<>();
        this.latch = new CountDownLatch(numberOfPeers);
        this.gui = gui;
    }

    public synchronized void add(List<FileSearchResult> result) {
        System.out.println("Adding results to shared list");
        for (FileSearchResult r : result) {
            for (FileSearchResult existing : results) {
                if (r.getFileName().equals(existing.getFileName()) || r.getFileHash().equals(existing.getFileHash())) {
                    existing.addNodeWithFile(new String[] {r.getNodeAddress(), String.valueOf(r.getNodePort())});
                    break;
                } else {
                    results.add(r);
                } 
            }
        }           
        latch.countDown(); // Decrement the latch count
        notifyAll(); // Notify any waiting threads
    }

    public void clear() {
        results.clear();
    }

    public void waitAndGetResults() throws InterruptedException {
        System.out.println("Waiting for final results...");
        try {
            latch.await(); // Wait until all peers have responded
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted: " + e.getMessage());
        }
        System.out.println("Final Results arrived - " + results);
        gui.updateSearchResults(results);
    }
}
