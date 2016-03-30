package sec.blockfs.blockclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import sec.blockfs.blocklibrary.BlockLibraryImpl;
import sec.blockfs.blocklibrary.InitializationFailureException;
import sec.blockfs.blockserver.FileSystemImpl;
import sec.blockfs.blockutility.BlockUtility;
import sec.blockfs.blockutility.DataIntegrityFailureException;
import sec.blockfs.blockutility.OperationFailedException;
import sec.blockfs.blockutility.WrongArgumentsException;

public class ClientDataBlockAttack {

    public static void main(String[] args) throws IOException, OperationFailedException, WrongArgumentsException {
        String servicePort = args[0];
        String serviceName = args[1];
        String serviceUrl = args[2];

        BlockLibraryImpl library = null;
        try {
            library = new BlockLibraryImpl(serviceName, servicePort, serviceUrl);
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
        String filePath = BlockUtility.BASE_PATH + File.separatorChar + fileName;
        
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

        } catch(DataIntegrityFailureException e) {
            System.out.println("Couldn't read data. "+e.getMessage());
        }         
    }
}
