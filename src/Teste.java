import java.io.IOException;

public class Teste {
    public static void main(String[] args) {
        try {
            ProcessBuilder pb1 = new ProcessBuilder("java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005", "IscTorrent", "1020", "disk");
            pb1.inheritIO().start();

            ProcessBuilder pb2 = new ProcessBuilder("java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006", "IscTorrent", "1021", "disk2");
            pb2.inheritIO().start();

            ProcessBuilder pb3 = new ProcessBuilder("java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5007", "IscTorrent", "1022", "disk4");
            pb3.inheritIO().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
