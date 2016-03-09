package sec.blockfs.blockclient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import sec.blockfs.blocklibrary.BlockLibrary;
import sec.blockfs.blocklibrary.InitializationFailureException;
import sec.blockfs.blockutility.BlockUtility;
import sec.blockfs.blockutility.OperationFailedException;

public class Client {
    public static void main(String[] args) throws IOException {
        String servicePort = args[0];
        String serviceName = args[1];
        String serviceUrl = args[2];

        BlockLibrary library = null;
        try {
            library = new BlockLibrary();
            library.FS_init(serviceName, servicePort, serviceUrl);
        } catch (InitializationFailureException e) {
            System.out.println("Error - " + e.getMessage());
            return;
        }

        try {
            String text = "Start_" + generateString(BlockUtility.BLOCK_SIZE) + "_End";
            System.out.println("Writing: ");
            System.out.println(text);
            System.out.println("################");
            System.out.println("Reading: ");
            System.out.println(text);
            byte[] textBytes = text.getBytes();
            library.FS_write(0, textBytes.length, textBytes);
            byte[] readBytes = new byte[textBytes.length];
            int bytesRead = library.FS_read(library.publicKey.getEncoded(), 0, textBytes.length, readBytes);

            if (bytesRead == textBytes.length && Arrays.equals(textBytes, readBytes))
                System.out.println("Read bytes are equals to written - success");
            else {
                System.out.println("Read bytes are different to written - failure");
                System.out.println("Sizes: Local = " + textBytes.length + "; Remote = " + bytesRead);
                System.out.println("Local: " + Arrays.toString(textBytes));
                System.out.println("Remote: " + Arrays.toString(readBytes));
            }
        } catch (OperationFailedException e) {
            e.printStackTrace();
        }

        System.in.read();
    }

    public static String generateString(int length) {
        String chars = new String("1234567890abcdefghijklmnopqrstuvxyz");// .-,/*-+@#£$%&()=?");
        Random rand = new Random();

        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = chars.charAt(rand.nextInt(chars.length()));
        }
        return new String(text);
    }
}
