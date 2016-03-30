package sec.blockfs.blockclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import sec.blockfs.blocklibrary.BlockLibraryImpl;
import sec.blockfs.blocklibrary.InitializationFailureException;
import sec.blockfs.blockutility.BlockUtility;
import sec.blockfs.blockutility.DataIntegrityFailureException;
import sec.blockfs.blockutility.OperationFailedException;
import sec.blockfs.blockutility.WrongArgumentsException;

public class ClientPublicBlockAttack {

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

        // get public key block
        String fileName = BlockUtility.getKeyString(BlockUtility.digest(library.publicKey.getEncoded()));
        String filePath = BlockUtility.BASE_PATH + File.separatorChar + fileName;
        FileInputStream stream = new FileInputStream(filePath);
        byte[] publicKeyBlock = new byte[BlockUtility.SIGNATURE_SIZE + BlockUtility.DIGEST_SIZE];
        stream.read(publicKeyBlock, 0, publicKeyBlock.length);
        stream.close();

        byte[] alteredTextBytes = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        byte[] alteredHash = BlockUtility.digest(alteredTextBytes);

        // write new block
        String newBlockName = BlockUtility.getKeyString(alteredHash);
        String newBlockPath = BlockUtility.BASE_PATH + File.separatorChar + newBlockName;
        FileOutputStream outStream = new FileOutputStream(newBlockPath);
        outStream.write(alteredTextBytes);
        outStream.close();

        // rewrite public key block to ignore the previous block and point to a new one
        byte[] rewrittenPublicKeyBlock = new byte[BlockUtility.SIGNATURE_SIZE + BlockUtility.DIGEST_SIZE];
        System.arraycopy(publicKeyBlock, 0, rewrittenPublicKeyBlock, 0, BlockUtility.SIGNATURE_SIZE);
        System.arraycopy(alteredHash, 0, rewrittenPublicKeyBlock, BlockUtility.SIGNATURE_SIZE, BlockUtility.DIGEST_SIZE);
        outStream = new FileOutputStream(filePath);
        outStream.write(rewrittenPublicKeyBlock);
        outStream.close();

        byte[] readBuffer = new byte[BlockUtility.BLOCK_SIZE];

        try {
            library.FS_read(library.publicKey.getEncoded(), 0, BlockUtility.BLOCK_SIZE, readBuffer);
        } catch (DataIntegrityFailureException e) {
            System.out.println("Couldn't read data. " + e.getMessage());
        }

        File blockDir = new File(BlockUtility.BASE_PATH + File.separatorChar);
        if (blockDir.exists())
            FileUtils.deleteDirectory(blockDir);
    }
}
