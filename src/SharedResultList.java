import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.Serializable;

public class SharedResultList implements Serializable {

    private CopyOnWriteArrayList<FileSearchResult> results;
    private transient CountDownLatch latch;
    private SearchGUI gui;

    public SharedResultList(int numberOfPeersWithFile, SearchGUI gui) {
        this.results = new CopyOnWriteArrayList<>();
        this.latch = new CountDownLatch(numberOfPeersWithFile);
        this.gui = gui;
    }

    public synchronized void add(List<FileSearchResult> result) {
        System.out.println("Adding results to shared list");
        
        for (FileSearchResult r : result) {
            if (results.isEmpty()) {
                results.addAll(result);
                break;
            }
            for (FileSearchResult existing : results) { // AQUI EM BAIXO VAI SER "&&" MAS PRECISAMOS CORRIGIR HASH
                if (r.getFileName() == existing.getFileName() || r.getFileHash().equals(existing.getFileHash())) {
                    existing.addNodeWithFile(new String[] {r.getNodeAddress(), String.valueOf(r.getNodePort())});
                    break;
                } else {
                    results.add(r);
                } 
            }
        }           
        latch.countDown(); // Decrement the latch count
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
