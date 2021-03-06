package sec.blockfs.blockserver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemException;

import sec.blockfs.blockutility.DataIntegrityFailureException;

public interface FileSystem {
    // writes contents
    String writeData(byte[] contents) throws IOException;
    // 
    String writePublicKey(byte[] contents, byte[] signature, byte[] publicKey) throws IOException;
    // reads the data block identified by 'block'
    byte[] read(String block) throws DataIntegrityFailureException, FileSystemException, FileNotFoundException;
}
