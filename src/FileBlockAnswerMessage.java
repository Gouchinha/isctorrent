import java.io.Serializable;

public class FileBlockAnswerMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private int fileHash;   // Identificador único do ficheiro (hash)
    private long blockOffset;  // Offset do início do bloco no ficheiro
    private byte[] blockData;  // Dados binários do bloco
    private int blockSize; // Tamanho real do bloco
    private String ipString;
    private int port;

    public FileBlockAnswerMessage(int fileHash, long blockOffset, byte[] blockData, String ipString, int port) {
        this.fileHash = fileHash;
        this.blockOffset = blockOffset;
        this.blockData = blockData;
        this.blockSize = blockData.length;
        this.port = port;
        this.ipString = ipString;
    }

    // Getters
    public int getFileHash() {
        return fileHash;
    }

    public String getIpString() {
        return ipString;
    }

    public int getPort(){
        return port;
    }

    public long getBlockOffset() {
        return blockOffset;
    }

    public byte[] getBlockData() {
        return blockData;
    }

    public int getBlockSize() {
        return blockSize;
    }

    // Representação em String para depuração
    @Override
    public String toString() {
        return "FileBlockAnswerMessage{" +
                "fileHash='" + fileHash + '\'' +
                ", blockOffset=" + blockOffset +
                ", blockSize=" + blockSize +
                '}';
    }
}
