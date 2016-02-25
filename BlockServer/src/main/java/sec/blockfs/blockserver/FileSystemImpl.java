package sec.blockfs.blockserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;

public class FileSystemImpl implements FileSystem {

  private static final String BASE_PATH = "C:\\Temp\\";

  public int FS_init(String pubKey) throws NoSuchAlgorithmException, NoSuchProviderException {
    File file = new File(BASE_PATH);
    if (!file.exists()) {
      file.mkdir();
    }
    return 0;
  }

  public void FS_write(String pubKey, int position, int size, byte[] contents) throws IOException {
    File file = new File(BASE_PATH + pubKey);
    if (!file.exists()) {
      file.createNewFile();
    }
    FileOutputStream stream = new FileOutputStream(BASE_PATH + pubKey);
    stream.write(contents, position, size);
  }

  public int FS_read(int id, int position, int size, byte[] buffer) {
    // TODO Auto-generated method stub
    return 0;
  }

}
