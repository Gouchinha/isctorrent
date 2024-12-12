import java.io.Serializable;

public class FileBlockRequestMessage implements Serializable  {

    private final byte[] fileHash;
    private final long offset;
    private final int length;

    public FileBlockRequestMessage(byte[] fileHash, long offset, int length) {
        this.fileHash = fileHash;
        this.offset = offset;
        this.length = length;
    }

    public byte[] getFileHash() {
        return fileHash;
    }

    public long getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }
    
}