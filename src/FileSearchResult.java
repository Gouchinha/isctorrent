import java.io.Serializable;

public class FileSearchResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private WordSearchMessage request;
    private String fileName;
    private long fileSize;
    private int fileHash;
    private String nodeAddress;
    private int nodePort;
    private ListStringVector nodesWithFile;

    public FileSearchResult(WordSearchMessage request, String fileName, long fileSize, int fileHash, String nodeAddress, int nodePort) {
        this.request = request;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileHash = fileHash;
        this.nodeAddress = nodeAddress;
        this.nodePort = nodePort;
        this.nodesWithFile = new ListStringVector();
        nodesWithFile.add(new String[] {nodeAddress, String.valueOf(nodePort)});
    }

    public WordSearchMessage getRequest() {
        return request;
    }

    public String[] addNodeWithFile(String[] node) {
        System.out.println("Adicionando nó com arquivo: " + node[0] + ":" + node[1]);
        nodesWithFile.add(node);
        return node;
    }

    public ListStringVector getNodesWithFile() {
        return nodesWithFile;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getFileHash() {
        return fileHash;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public int getNodePort() {
        return nodePort;
    }

    @Override
    public String toString() {
        return fileName + " (Hash: " + fileHash + ", Tamanho: " + fileSize + " bytes) em " + nodeAddress + ":" + nodePort;
    }

    
}

