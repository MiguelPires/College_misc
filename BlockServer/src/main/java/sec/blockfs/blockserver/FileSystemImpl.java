package sec.blockfs.blockserver;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;

public class FileSystemImpl implements FileSystem {
  
  private Signature signature;
  
  public int FS_init(String pubKey) throws NoSuchAlgorithmException, NoSuchProviderException {
    return 0;
  }

  public void FS_write(String pubKey, int position, int size, byte[] contents) {
    

  }

  public int FS_read(int id, int position, int size, byte[] buffer) {
    // TODO Auto-generated method stub
    return 0;
  }

}
