import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchGUI extends JFrame {
    private JTextField searchField, addressField, portField;
    private JButton searchButton, downloadButton, connectButton;
    private JList<String> resultList;
    private DefaultListModel<FileSearchResult> listModel;
    private FileNode fileNode; // Referência para o nó

    public SearchGUI(FileNode fileNodeObject) {
        setTitle("Pesquisa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 350);
        setLocationRelativeTo(null);

        listModel = new DefaultListModel<FileSearchResult>();
        this.fileNode = fileNodeObject;

        resultList = new JList<>();
        JScrollPane listScrollPane = new JScrollPane(resultList);

        // Painel de pesquisa
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        searchButton = new JButton("Procurar");
        searchPanel.add(new JLabel("Texto a procurar:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        // Painel de botões à direita
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        downloadButton = new JButton("Descarregar");
        connectButton = new JButton("Ligar a Nó");
        buttonPanel.add(downloadButton);
        buttonPanel.add(connectButton);

        // Adiciona componentes à janela
        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(searchPanel, BorderLayout.NORTH);
        getContentPane().add(listScrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.EAST);

        // Configuração de botões
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String searchText = searchField.getText();
                // Envia a procura para os nós conectados
                fileNode.sendWordSearchRequest(searchText);
            }
        });

        downloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = resultList.getSelectedIndex();
                FileSearchResult selected = selectedIndex != -1 ? listModel.getElementAt(selectedIndex) : null;
                if (selected != null) {
                    fileNode.startDownload(selected); // Inicia o download do ficheiro selecionado
                    JOptionPane.showMessageDialog(null, "Descarregar: " + selected.getFileName());
                } else {
                    JOptionPane.showMessageDialog(null, "Nenhum ficheiro selecionado!");
                }
            }
        });

        connectButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // Criar a nova janela
                JFrame conectFrame = new JFrame("Conectar a nó");
                conectFrame.setSize(300, 150);
                conectFrame.setLayout(new BorderLayout());
                conectFrame.setResizable(false);
                conectFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Fecha apenas esta janela
                conectFrame.setLocationRelativeTo(SearchGUI.this); // Centraliza em relação à janela principal

                // Painel central para os campos de entrada
                JPanel conectPanel = new JPanel(new GridLayout(2, 2, 5, 5));
                JLabel addressLabel = new JLabel("Endereço:");
                addressField = new JTextField("localhost", 10);
                JLabel portLabel = new JLabel("Porta:");
                portField = new JTextField("", 5); // Trocar JTextArea por JTextField (mais apropriado)

                conectPanel.add(addressLabel);
                conectPanel.add(addressField);
                conectPanel.add(portLabel);
                conectPanel.add(portField);

                conectFrame.add(conectPanel, BorderLayout.CENTER);

                // Painel para os botões
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                JButton okButton = new JButton("OK");
                JButton cancelButton = new JButton("Cancelar");

                // Ação do botão "OK"
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        try {
                            String address = addressField.getText();
                            int port = Integer.parseInt(portField.getText());
                            // Simulação da conexão (completa o que está comentado no futuro)
                            fileNode.connectToPeer(address, port);
                            //JOptionPane.showMessageDialog(conectFrame, "Ligado a: " + address + " na porta " + port);
                            conectFrame.dispose(); // Fecha a janela após o sucesso
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(conectFrame, "Insira uma porta válida!", "Erro",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                // Ação do botão "Cancelar"
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        addressField.setText("");
                        portField.setText("");
                        conectFrame.dispose(); // Fecha a janela ao cancelar
                    }
                });

                buttonPanel.add(okButton);
                buttonPanel.add(cancelButton);
                conectFrame.add(buttonPanel, BorderLayout.SOUTH);

                // Torna a janela visível
                conectFrame.setVisible(true);
            }
        });
    }

    // Método para atualizar a lista de resultados de pesquisa
    public void updateSearchResults(List<FileSearchResult> results) {
    listModel.clear(); // Limpa os resultados anteriores
    
    if (results.isEmpty()) {
        // JOptionPane.showMessageDialog(this, "Nenhum resultado encontrado!");
    } else {
        /* // Map para filtrar elementos únicos por hash e nome
        Map<String, FileSearchResult> filteredResults = new HashMap<>();
        for (FileSearchResult result : results) {
            String key = result.getFileHash() + ":" + result.getFileName(); // Combinação de hash e nome como chave
            if (!filteredResults.containsKey(key)) {
                filteredResults.put(key, result); // Adiciona ao mapa apenas se ainda não existir
            }
        } */

        // Adiciona os resultados filtrados ao listModel
        for (FileSearchResult result : results) {
            listModel.addElement(result);
        }
    }

    // Atualiza o modelo da lista
    resultList.setModel(new AbstractListModel<String>() {
        @Override
        public int getSize() {
            return listModel.getSize();
        }

        @Override
        public String getElementAt(int index) {
            return listModel.getElementAt(index).getFileName();
        }
    });
    }

}
