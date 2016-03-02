package sec.blockfs.blockserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

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
    public void write(byte[] contents) throws FileSystemException  {
        try {
            byte[] dataDigest = BlockUtility.digest(contents);
            String fileName = BlockUtility.getKeyString(dataDigest);
            String filePath = BASE_PATH + File.separatorChar + fileName;

            System.out.println("Writing data block: " + filePath);
            FileOutputStream stream = new FileOutputStream(filePath);
            stream.write(contents);

            stream.close();
        } catch (IOException e) {
            System.out.println("Filesystem error - write operation failed" + e.getMessage());
            throw new FileSystemException("Write operation failed");
        }
    }

    @Override
    public byte[] read(String blockName) throws DataIntegrityFailureException, FileSystemException {
        byte[] dataBlock;
        
        try {
            String filePath = BASE_PATH + File.separatorChar + blockName;
            FileInputStream stream = new FileInputStream(filePath);
            
            Path path = Paths.get(filePath);
            dataBlock = Files.readAllBytes(path);
            stream.close();
        } catch (Exception e) {
            throw new FileSystemException("Unable to read block "+blockName);
        }
        
        byte[] dataDigest = BlockUtility.digest(dataBlock);
        String expectedFilename = BlockUtility.getKeyString(dataDigest);
        
        if (!blockName.equals(expectedFilename))
            throw new DataIntegrityFailureException("The stored data has been changed");
        
        return dataBlock;
    }

}
