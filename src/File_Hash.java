import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class File_Hash {

    private File file;
    private byte[] hash;

    public File_Hash(File file) throws IOException {
        this.file = file;
        this.hash = calculateHash(file);
    }

    public File getFile() {
        return file;
    }

    public byte[] getHash() {
        return hash;
    }

    public String getName() {
        return file.getName();
    }

    public static byte[] calculateHash(File file) throws IOException {
        byte[] fileData = Files.readAllBytes(file.toPath());
        return computeSHA256(fileData);
    }

    private static byte[] computeSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        File_Hash fileHash = (File_Hash) obj;
        return Arrays.equals(hash, fileHash.hash) && file.getName().equals(fileHash.file.getName());
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(hash);
        result = 31 * result + file.getName().hashCode();
        return result;
    }
}
