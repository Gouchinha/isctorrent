import java.util.List;
//import java.nio.file.Path;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;


public class SendDownloadThread extends Thread {

    private FileBlockRequestMessage message;
    private String directoryPath;
    private SharedSendDownload sharedSendDownload;

    public SendDownloadThread(FileBlockRequestMessage message, String directoryPath, SharedSendDownload sharedSendDownload) {
        System.out.println("SendDownloadThread initiated");
        super();
        this.message = message;
        this.directoryPath = directoryPath;
        this.sharedSendDownload = sharedSendDownload;
    }

    @Override
    public void run() {
        System.out.println("SendDownloadThread running");
        while (true) {
            FileBlockRequestMessage request = sharedSendDownload.getNextBlockRequest();
            System.out.println("Request received: " + request.getOffset());
            
            processRequest(request);

            if (request.isLastBlock() == true) {
                this.interrupt();
            }
        }
    }

    public void processRequest(FileBlockRequestMessage request) {
        byte[] fileHash = request.getFileHash();
        long offset = request.getOffset();
        int length = request.getLength();
        File file = new File(directoryPath);
        byte[] data = new byte[length];
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(offset);
            raf.read(data, 0, length);
            raf.close();
            
            // Create and send FileBlockAnswerMessage
            FileBlockAnswerMessage answerMessage = new FileBlockAnswerMessage(fileHash, offset, data);
            System.out.println("Block answer created: " + answerMessage.getBlockOffset());
            
            //sharedSendDownload.sendBlockAnswer(answerMessage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendBlockAnswer(FileBlockAnswerMessage answerMessage) {

        System.out.println("Block answer sent: " + answerMessage.getBlockOffset());
    }   
}   
