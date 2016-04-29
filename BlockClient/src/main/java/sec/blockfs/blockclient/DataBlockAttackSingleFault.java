package sec.blockfs.blockclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;

import sec.blockfs.blocklibrary.BlockLibraryImpl;
import sec.blockfs.blocklibrary.InitializationFailureException;
import sec.blockfs.blockserver.FileSystemImpl;
import sec.blockfs.blockutility.BlockUtility;
import sec.blockfs.blockutility.DataIntegrityFailureException;

public class DataBlockAttackSingleFault {

    public static void main(String[] args) throws Exception {
        String servicePort = args[0];
        String serviceName = args[1];
        String serviceUrl = args[2];
        String numFaults = args[3];

        BlockLibraryImpl library = null;
        try {
            library = new BlockLibraryImpl(serviceName, servicePort, serviceUrl, numFaults);
            library.FS_init();
        } catch (InitializationFailureException e) {
            System.out.println("Error - " + e.getMessage());
            return;
        }

        String text = BlockUtility.generateString(BlockUtility.BLOCK_SIZE);
        byte[] textBytes = text.getBytes();

        library.FS_write(0, textBytes.length, textBytes);

        // give some time to the server threads to write the blocks
        // otherwise we might not find them
        Thread.sleep(1000);
        
        // get data block
        String fileName = BlockUtility.getKeyString(BlockUtility.digest(textBytes));

        int changesCounter = 0;
        for (int i = 0; i < BlockLibraryImpl.NUM_REPLICAS_HASH && changesCounter < 3; ++i) {
            String filePath = FileSystemImpl.BASE_PATH + "-" + i + File.separatorChar + fileName;
            FileInputStream stream;
            try {
                stream = new FileInputStream(filePath);
                changesCounter++;
            } catch (FileNotFoundException e) {
                // if didn't find the file it's possible that this replica wasn't part of the quorum
                continue;
            }

            byte[] dataBlock = new byte[BlockUtility.BLOCK_SIZE];
            stream.read(dataBlock, 0, dataBlock.length);
            stream.close();

            // change data block on server
            byte[] alteration = "altered".getBytes();
            System.arraycopy(alteration, 0, dataBlock, 0, alteration.length);

            FileOutputStream outStream = new FileOutputStream(filePath);
            outStream.write(dataBlock);
            outStream.close();
        }

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
