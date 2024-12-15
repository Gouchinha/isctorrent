import java.io.Serializable;

public class FileBlockRequestMessage implements Serializable  {

    private final int fileHash;
    private final long offset;
    private final int length;
    private int downloadIdentifier;

    public FileBlockRequestMessage(int fileHash, long offset, int length, int downloadIdentifier, boolean isLastBlock) {
        this.fileHash = fileHash;
        this.offset = offset;
        this.length = length;
        this.downloadIdentifier = downloadIdentifier;
    }

    public int getFileHash() {
        return fileHash;
    }

    public int getDownloadIdentifier() {
        return downloadIdentifier;
    }

    public boolean isLastBlock() {
        return false;
    }   

    public long getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    /* public String toString() {
        return "FileBlockRequestMessage{" +
                "fileHash=" + fileHash +
                ", offset=" + offset +
                ", length=" + length +
                ", downloadIdentifier=" + downloadIdentifier +
                '}';
    } */
    
}