import java.io.Serializable;

public class FileBlockAnswerMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private byte[] fileHash;   // Identificador único do ficheiro (hash)
    private long blockOffset;  // Offset do início do bloco no ficheiro
    private byte[] blockData;  // Dados binários do bloco
    private int blockSize;     // Tamanho real do bloco

    public FileBlockAnswerMessage(byte[] fileHash, long blockOffset, byte[] blockData) {
        this.fileHash = fileHash;
        this.blockOffset = blockOffset;
        this.blockData = blockData;
        this.blockSize = blockData.length;
    }

    // Getters
    public byte[] getFileHash() {
        return fileHash;
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
