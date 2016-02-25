package sec.blockfs.blockserver;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public interface FileSystem {
  // initializes the file system
  int FS_init(String pubKey) throws NoSuchAlgorithmException, NoSuchProviderException;

  // writes the 'contents' of length 'size' from the 'pos' position
  void FS_write(String pubKey, int position, int size, byte[] contents) throws IOException;

  // reads the data block identified by 'id'; returns the number of bytes read
  int FS_read(int id, int position, int size, byte[] buffer);
}
