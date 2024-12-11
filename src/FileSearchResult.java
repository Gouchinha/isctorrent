import java.io.Serializable;
import java.net.InetAddress;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class FileSearchResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private WordSearchMessage request;
    private String fileName;
    private long fileSize;
    private byte[] fileHash;
    private String nodeAddress;
    private int nodePort;
    private List<String[]> nodesWithFile;

    public FileSearchResult(WordSearchMessage request, String fileName, long fileSize, byte[] fileHash, String nodeAddress, int nodePort) {
        this.request = request;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileHash = fileHash;
        this.nodeAddress = nodeAddress;
        this.nodePort = nodePort;
        this.nodesWithFile = new ArrayList<String[]>();
    }

    public WordSearchMessage getRequest() {
        return request;
    }

    public String[] addNodeWithFile(String[] node) {
        nodesWithFile.add(node);
        return node;
    }

    public List<String[]> getNodeswithFile() {
        return nodesWithFile;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public byte[] getFileHash() {
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

