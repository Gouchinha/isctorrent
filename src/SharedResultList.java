import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.swing.DefaultListModel;
import javax.swing.AbstractListModel;
import javax.swing.JList;

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
        results.addAll(result);
        latch.countDown(); // Decrement the latch count
    }

    public void clear() {
        results.clear();
    }

    public void waitAndGetResults() throws InterruptedException {
        try {
            latch.await(); // Wait until all peers have responded
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted: " + e.getMessage());
        }
        gui.updateSearchResults(results);
    }
}
