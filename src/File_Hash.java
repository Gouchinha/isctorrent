import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class File_Hash {

    File file;
    byte[] hash;

    public File_Hash(File file) throws IOException {
        this.file = file;
        this.hash = getHash(getData(file));
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

    public byte[] getData(File file) throws IOException {
        byte[] fileContents = Files.readAllBytes(file.toPath());
        return fileContents;
    }

    public byte[] getHash(byte[] fileData) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(fileData);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }
}
