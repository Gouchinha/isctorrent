import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileLoader {
    private String directoryPath;
    private List<File_Hash> files;

    public FileLoader(String pastaDownload) throws IOException {
        this.directoryPath = Path.of("").toAbsolutePath().resolve(pastaDownload).toString();

        this.files = new ArrayList<File_Hash>();
        loadFiles();
    }

    // Método para carregar ficheiros e armazená-los na lista de files
    private void loadFiles() throws IOException {
    File directory = new File(directoryPath);
    if (isValidDirectory(directory)) {
        loadFilesFromDirectory(directory);
    } else {
        System.out.println("Diretório não encontrado: " + directoryPath);
    }
}

private boolean isValidDirectory(File directory) {
    return directory.exists() && directory.isDirectory();
}

private void loadFilesFromDirectory(File directory) {
    File[] fileArray = directory.listFiles();
    if (fileArray != null) {
        for (File file : fileArray) {
            if (file.isFile()) {
                try {
                    files.add(new File_Hash(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

    // Retorna a lista de objetos File
    public List<File_Hash> getFiles() {
        return files;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    // Retorna uma lista com os nomes dos ficheiros
    public List<String> getFileNames() {
        List<String> fileNames = new ArrayList<>();
        for (File_Hash file : files) {
            fileNames.add(file.getName());
        }
        return fileNames;
    }
}

