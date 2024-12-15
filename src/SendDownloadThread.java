//import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;


public class SendDownloadThread extends Thread {

    private String directoryPath;
    private SharedSendDownload sharedSendDownload;
    private FileNode fileNode;

    public SendDownloadThread(FileBlockRequestMessage message, String directoryPath, SharedSendDownload sharedSendDownload) {
        System.out.println("SendDownloadThread initiated");
        super();
        this.directoryPath = directoryPath;
        this.sharedSendDownload = sharedSendDownload;
    }

    @Override
    public void run() {
        System.out.println("SendDownloadThread running");

    }
    
       
}   
