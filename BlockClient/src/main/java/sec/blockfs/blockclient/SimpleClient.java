package sec.blockfs.blockclient;

import java.io.IOException;
import java.util.Arrays;

import sec.blockfs.blocklibrary.BlockLibrary;
import sec.blockfs.blocklibrary.InitializationFailureException;
import sec.blockfs.blockserver.DataIntegrityFailureException;
import sec.blockfs.blockutility.BlockUtility;
import sec.blockfs.blockutility.OperationFailedException;

public class SimpleClient {
    public static void main(String[] args) throws IOException {
        String servicePort = args[0];
        String serviceName = args[1];
        String serviceUrl = args[2];

        BlockLibrary library = null;
        try {
            library = new BlockLibrary(serviceName, servicePort, serviceUrl);
            library.FS_init();
        } catch (InitializationFailureException e) {
            System.out.println("Error - " + e.getMessage());
            return;
        }
        
        try {
            String text = BlockUtility.generateString(BlockUtility.BLOCK_SIZE-1);
            System.out.println("Writing: ");
            System.out.println(text);
            System.out.println("################");

            byte[] textBytes = text.getBytes();
            library.FS_write(0, textBytes.length, textBytes);
            byte[] readBytes = new byte[textBytes.length];
            int bytesRead = library.FS_read(library.publicKey.getEncoded(), 0, textBytes.length, readBytes);

            System.out.println("Reading: ");
            String readString = new String(readBytes);
            System.out.println(readString);

            if (bytesRead == textBytes.length && Arrays.equals(textBytes, readBytes) && text.equals(readString))
                System.out.println("Read bytes are equals to written - success");
            else {
                System.out.println("Read bytes are different to written - failure");
                System.out.println("Sizes: Local = " + textBytes.length + "; Remote = " + bytesRead);
                System.out.println("Local: " + Arrays.toString(textBytes));
                System.out.println("Remote: " + Arrays.toString(readBytes));
            }
        } catch (OperationFailedException | DataIntegrityFailureException e) {
            e.printStackTrace();
        }

        System.in.read();
    }

}
