import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;

public class IscTorrent {
    public static void main(String[] args) {

        if (args.length == 0) {
            String input = JOptionPane.showInputDialog(null, "Por favor, insira os argumentos separados por espaço:");
            if (input != null) {
                args = input.split(" ");
            } else {
                System.out.println("Nenhum argumento fornecido.");
                System.exit(1);
            }
        }

        if (args.length != 2) {
            JOptionPane.showMessageDialog(null, "Argumentos Insuficientes. Uso: <porta> <diretoriaDownload>");
            System.exit(1);
        }

        try {
            // Obter os argumentos
            int porta = Integer.parseInt(args[0]);
            String pastaDownload = args[1];

            // Validar os argumentos
            if (porta < 1 || porta > 65535) {
                throw new IllegalArgumentException("A porta deve estar entre 1 e 65535.");
            }

            // Lógica principal da aplicação
            System.out.println("Iniciando IscTorrent na porta " + porta);
            System.out.println("Diretoria de download: " + pastaDownload);

            // Iniciar servidor
            startServer(porta, pastaDownload);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Erro: A porta deve ser um número inteiro.");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void startServer(int porta, String pastaDownload) {
        // Iniciar o servidor numa nova thread
        SwingUtilities.invokeLater(() -> {
            try {
                FileNode node = new FileNode(porta, pastaDownload);
                System.out.println("Servidor iniciado com sucesso!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Erro ao iniciar o servidor: " + e.getMessage());
                System.exit(1);
            }
        });
    }
}