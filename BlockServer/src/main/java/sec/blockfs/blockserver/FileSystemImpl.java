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

import sec.blockfs.blockutility.BlockUtility;

public class FileSystemImpl implements FileSystem {

    public static final String BASE_PATH = "C:\\Temp";

    public FileSystemImpl() {
        File file = new File(BASE_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    @Override
    public String writeData(byte[] contents) throws FileSystemException {
        try {
            int dataSize = contents.length-BlockUtility.SIGNATURE_SIZE;
            byte[] data = new byte[dataSize];
            System.arraycopy(contents, BlockUtility.SIGNATURE_SIZE, data, 0, dataSize);
            
            byte[] dataDigest = BlockUtility.digest(data);
            String fileName = BlockUtility.getKeyString(dataDigest);
            String filePath = BASE_PATH + File.separatorChar + fileName;
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

    @Override
    public String writePublicKey(byte[] dataHash, byte[] signature, byte[] publicKey) throws FileSystemException {
        try {
            String fileName = BlockUtility.getKeyString(BlockUtility.digest(publicKey));
            String filePath = BASE_PATH + File.separatorChar + fileName;
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

    @Override
    public byte[] read(String blockName) throws DataIntegrityFailureException, FileSystemException {
        byte[] dataBlock;

            String filePath = BASE_PATH + File.separatorChar + blockName;
            try {
                FileInputStream stream = new FileInputStream(filePath);

            Path path = Paths.get(filePath);
            dataBlock = Files.readAllBytes(path);
            stream.close();
            } catch (FileNotFoundException e) {
                return null;
            } catch(IOException e){
                throw new FileSystemException("File system error when finding file: "+blockName);
            }
        return dataBlock;
    }
}
