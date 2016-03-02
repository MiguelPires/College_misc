package sec.blockfs.blockserver;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public interface FileSystem {
    // writes contents
    void write(byte[] contents) throws IOException;

    // reads the data block identified by 'block'
    byte[] read(String block) throws DataIntegrityFailureException, FileSystemException;
}
