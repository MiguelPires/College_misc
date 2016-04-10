package sec.blockfs.blockclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

import sec.blockfs.blocklibrary.BlockLibrary;
import sec.blockfs.blocklibrary.InitializationFailureException;
import sec.blockfs.blockserver.DataIntegrityFailureException;
import sec.blockfs.blockserver.FileSystemImpl;
import sec.blockfs.blockutility.BlockUtility;

public class DataBlockAttackSingleFault {

    public static void main(String[] args) throws Exception {
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

        String text = BlockUtility.generateString(BlockUtility.BLOCK_SIZE);
        byte[] textBytes = text.getBytes();

        library.FS_write(0, textBytes.length, textBytes);

        // get data block
        String fileName = BlockUtility.getKeyString(BlockUtility.digest(textBytes));
        String filePath = FileSystemImpl.BASE_PATH + "-" + 0 + File.separatorChar + fileName;

        FileInputStream stream = new FileInputStream(filePath);
        byte[] dataBlock = new byte[BlockUtility.BLOCK_SIZE];
        stream.read(dataBlock, 0, dataBlock.length);
        stream.close();

        // change data block
        byte[] alteration = "altered".getBytes();
        System.arraycopy(alteration, 0, dataBlock, 0, alteration.length);

        FileOutputStream outStream = new FileOutputStream(filePath);
        outStream.write(dataBlock);
        outStream.close();

        byte[] readBuffer = new byte[BlockUtility.BLOCK_SIZE];
        try {
            library.FS_read(library.publicKey.getEncoded(), 0, BlockUtility.BLOCK_SIZE, readBuffer);
            assert Arrays.equals(textBytes, readBuffer) : "Read data different from expected";
            System.out.println("Successfuly read data.");
        } catch (DataIntegrityFailureException e) {
            System.out.println("Couldn't read data. " + e.getMessage());
        }
    }
}
