package sec.blockfs.blockserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import sec.blockfs.blockutility.BlockUtility;

public class FileSystemImpl implements FileSystem {

    private File file;

    public FileSystemImpl() {
        file = new File(BlockUtility.BASE_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public String writeData(byte[] contents) throws FileSystemException {
        try {
            if (!file.exists()) {
                file.mkdir();
            }

            byte[] dataDigest = BlockUtility.digest(contents);
            String fileName = BlockUtility.getKeyString(dataDigest);
            String filePath = BlockUtility.BASE_PATH + File.separatorChar + fileName;
            System.out.println("Writing data block: " + filePath);

            FileOutputStream stream = new FileOutputStream(filePath);
            stream.write(contents);
            stream.close();
            return fileName;
        } catch (IOException e) {
            System.out.println("Filesystem error - write operation failed" + e.getMessage());
            throw new FileSystemException("Write operation failed");
        }
    }

    public String writePublicKey(byte[] dataHash, byte[] signature, byte[] publicKey) throws FileSystemException {
        try {
            if (!file.exists()) {
                file.mkdir();
            }

            String fileName = BlockUtility.getKeyString(BlockUtility.digest(publicKey));
            String filePath = BlockUtility.BASE_PATH + File.separatorChar + fileName;
            System.out.println("Writing public key block: " + filePath);

            // concatenate data
            byte[] publicKeyBlock = new byte[signature.length + dataHash.length];
            System.arraycopy(signature, 0, publicKeyBlock, 0, signature.length);
            System.arraycopy(dataHash, 0, publicKeyBlock, signature.length, dataHash.length);

            // write block
            FileOutputStream stream = new FileOutputStream(filePath);
            stream.write(publicKeyBlock, 0, publicKeyBlock.length);
            stream.close();

            return fileName;
        } catch (IOException e) {
            System.out.println("Filesystem error - write operation failed" + e.getMessage());
            throw new FileSystemException("Write operation failed");
        }
    }

    public byte[] read(String blockName) throws FileSystemException, FileNotFoundException {
        byte[] dataBlock;
        System.out.println("Reading block: " + blockName);

        String filePath = BlockUtility.BASE_PATH + File.separatorChar + blockName;

        FileInputStream stream = new FileInputStream(filePath);

        try {
            Path path = Paths.get(filePath);
            dataBlock = Files.readAllBytes(path);
            stream.close();
        } catch (IOException e) {
            throw new FileSystemException("File system error when finding file: " + blockName);
        }

        return dataBlock;
    }
}
