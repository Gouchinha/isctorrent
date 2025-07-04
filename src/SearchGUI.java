import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class SearchGUI extends JFrame {
    private JTextField searchField, addressField, portField;
    private JButton searchButton, downloadButton, connectButton, showPeersButton; // Add this line to declare the new button
    private JList<String> resultList;
    private DefaultListModel<FileSearchResult> listModel;
    private FileNode fileNode; // Referência para o nó

    public SearchGUI(FileNode fileNodeObject) {
        setTitle("Isctorrent - Porto: " + fileNodeObject.getPort());
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
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5)); // Adjust the grid layout to 3 rows
        downloadButton = new JButton("Descarregar");
        connectButton = new JButton("Ligar a Nó");
        showPeersButton = new JButton("Mostrar Peers"); // Initialize the new button
        buttonPanel.add(downloadButton);
        buttonPanel.add(connectButton);
        buttonPanel.add(showPeersButton); // Add the new button to the button panel

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
                JFrame connectFrame = new JFrame("Conectar a nó");
                connectFrame.setSize(300, 150);
                connectFrame.setLayout(new BorderLayout());
                connectFrame.setResizable(false);
                connectFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Fecha apenas esta janela
                connectFrame.setLocationRelativeTo(SearchGUI.this); // Centraliza em relação à janela principal

                // Painel central para os campos de entrada
                JPanel connectPanel = new JPanel(new GridLayout(2, 2, 5, 5));
                JLabel addressLabel = new JLabel("Endereço:");
                addressField = new JTextField("127.0.0.1", 10);
                JLabel portLabel = new JLabel("Porta:");
                portField = new JTextField("", 5); // Trocar JTextArea por JTextField (mais apropriado)

                connectPanel.add(addressLabel);
                connectPanel.add(addressField);
                connectPanel.add(portLabel);
                connectPanel.add(portField);

                connectFrame.add(connectPanel, BorderLayout.CENTER);

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
                            //JOptionPane.showMessageDialog(connectFrame, "Ligado a: " + address + " na porta " + port);
                            connectFrame.dispose(); // Fecha a janela após o sucesso
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(connectFrame, "Insira uma porta válida!", "Erro",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                // Ação do botão "Cancelar"
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        addressField.setText("");
                        portField.setText("");
                        connectFrame.dispose(); // Fecha a janela ao cancelar
                    }
                });

                buttonPanel.add(okButton);
                buttonPanel.add(cancelButton);
                connectFrame.add(buttonPanel, BorderLayout.SOUTH);

                // Torna a janela visível
                connectFrame.setVisible(true);
            }
        });

        showPeersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                List<SocketAndStreams> connectedPeers = fileNode.getConnectedPeers();
                StringBuilder peersInfo = new StringBuilder("Peers conectados:\n");
                for (SocketAndStreams peer : connectedPeers) {
                    peersInfo.append(peer.getIpString()).append(":").append(peer.getNodePort()).append("\n");
                }
                JOptionPane.showMessageDialog(null, peersInfo.toString(), "Peers Conectados", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    // Método para atualizar a lista de resultados de pesquisa
    public void updateSearchResults(List<FileSearchResult> results) {
    listModel.clear(); // Limpa os resultados anteriores
    
    if (results.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Nenhum resultado encontrado!");
    } else {
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
            return listModel.getElementAt(index).getFileName() + " (" + listModel.getElementAt(index).getNodesWithFile().toString();
        }
    });
    }

}
