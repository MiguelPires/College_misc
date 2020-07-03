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
import java.security.NoSuchAlgorithmException;

import sec.blockfs.blockutility.BlockUtility;

public class FileSystemImpl implements FileSystem {

    public static final String BASE_PATH = "C:\\Temp";
    public String serverId;

    public FileSystemImpl(String serverId) {
        this.serverId = serverId;
        File file = new File(BASE_PATH + "-" + serverId);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    @Override
    public synchronized String writeData(byte[] contents) throws FileSystemException {
        try {
            byte[] dataDigest = BlockUtility.digest(contents);
            String fileName = BlockUtility.getKeyString(dataDigest);
            String filePath = BASE_PATH + "-" + serverId + File.separatorChar + fileName;
            System.out.println("Writing data block: " + filePath);

            FileOutputStream stream = new FileOutputStream(filePath);
            stream.write(contents);
            stream.close();
            return fileName;
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println("Filesystem error - write operation failed" + e.getMessage());
            throw new FileSystemException("Write operation failed");
        }
    }

    @Override
    public synchronized String writePublicKey(byte[] dataHash, byte[] signature, byte[] publicKey) throws FileSystemException {
        try {
            String fileName = BlockUtility.getKeyString(BlockUtility.digest(publicKey));
            String filePath = BASE_PATH + "-" + serverId + File.separatorChar + fileName;
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
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println("Filesystem error - write operation failed" + e.getMessage());
            throw new FileSystemException("Write operation failed");
        }
    }

    @Override
    public synchronized byte[] read(String blockName) throws FileSystemException, FileNotFoundException {
        byte[] dataBlock;

        String filePath = BASE_PATH + "-" + serverId + File.separatorChar + blockName;
        System.out.println("Reading block: " + filePath);
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
