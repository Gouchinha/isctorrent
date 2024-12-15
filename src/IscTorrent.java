import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;

public class IscTorrent {
    public static void main(String[] args) {

        args = getArgsFromInput(args);

        if (args.length != 2) {
            showInsufficientArgsMessage();
            System.exit(1);
        }

        try {
            // Obter os argumentos
            int porta = Integer.parseInt(args[0]);
            String pastaDownload = args[1];

            validateArgs(porta, pastaDownload);

            // Lógica principal da aplicação
            System.out.println("Iniciando IscTorrent na porta " + porta);
            System.out.println("Diretoria de download: " + pastaDownload);

            // Iniciar servidor
            startServer(porta, pastaDownload);

        } catch (NumberFormatException e) {
            showNumberFormatErrorMessage();
            System.exit(1);
        } catch (IllegalArgumentException e) {
            showIllegalArgumentErrorMessage(e);
            System.exit(1);
        }
    }

    private static String[] getArgsFromInput(String[] args) {
        if (args.length == 0) {
            String input = JOptionPane.showInputDialog(null, "Por favor, insira os argumentos separados por espaço:");
            if (input != null) {
                return input.split(" ");
            } else {
                System.out.println("Nenhum argumento fornecido.");
                System.exit(1);
            }
        }
        return args;
    }

    private static void showInsufficientArgsMessage() {
        JOptionPane.showMessageDialog(null, "Argumentos Insuficientes. Uso: <porta> <diretoriaDownload>");
    }

    private static void validateArgs(int porta, String pastaDownload) {
        if (porta < 1 || porta > 65535) {
            throw new IllegalArgumentException("A porta deve estar entre 1 e 65535.");
        }
    }

    private static void showNumberFormatErrorMessage() {
        JOptionPane.showMessageDialog(null, "Erro: A porta deve ser um número inteiro.");
    }

    private static void showIllegalArgumentErrorMessage(IllegalArgumentException e) {
        JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
    }

    private static void startServer(int porta, String pastaDownload) {
        // Iniciar o servidor numa nova thread
        SwingUtilities.invokeLater(() -> {
            try {
                new FileNode(porta, pastaDownload);
                System.out.println("Servidor iniciado com sucesso!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Erro ao iniciar o servidor: " + e.getMessage());
                System.exit(1);
            }
        });
    }
}