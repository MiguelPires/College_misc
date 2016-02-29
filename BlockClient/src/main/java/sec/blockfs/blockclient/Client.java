package sec.blockfs.blockclient;

import java.io.IOException;

import sec.blockfs.blocklibrary.BlockLibrary;
import sec.blockfs.blocklibrary.OperationFailedException;

public class Client {
    public static void main(String[] args) throws IOException {
        String servicePort = args[0];
        String serviceName = args[1];
        String serviceUrl = args[2];

        BlockLibrary library = null;
        try {
            library = new BlockLibrary();
            library.FS_init(serviceName, servicePort, serviceUrl);
        } catch (Exception e) {
            System.out.println("Error - couldn't instantiate library");
            e.printStackTrace();
            return;
        }

        try {
            String text = "Some random string";
            byte[] textBytes = text.getBytes();
            library.FS_write(0, textBytes.length, textBytes);
        } catch (OperationFailedException e) {
            e.printStackTrace();
        }
        
        System.in.read();
    }
}
