import java.io.Serializable;

public class FileBlockRequestMessage implements Serializable  {

    private final byte[] fileHash;
    private final long offset;
    private final int length;
    private int downloadIdentifier;

    public FileBlockRequestMessage(byte[] fileHash, long offset, int length, int downloadIdentifier) {
        this.fileHash = fileHash;
        this.offset = offset;
        this.length = length;
        this.downloadIdentifier = downloadIdentifier;
    }

    public byte[] getFileHash() {
        return fileHash;
    }

    public int getDownloadIdentifier() {
        return downloadIdentifier;
    }

    public long getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }
    
}