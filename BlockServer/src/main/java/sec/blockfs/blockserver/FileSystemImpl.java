package sec.blockfs.blockserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;

public class FileSystemImpl implements FileSystem {

  public static final String BASE_PATH = "C:\\Temp";

  public int FS_init() {
    File file = new File(BASE_PATH);
    if (!file.exists()) {
      file.mkdir();
    }
    return 0;
  }

  public void FS_write(int position, int size, byte[] contents) throws IOException {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-512");
      md.update(contents);
      byte[] digest = md.digest();
      String fileName = Base64.getEncoder().encode(digest).toString();

      System.out.println("Writing data block: " + BASE_PATH + File.separatorChar + fileName);
      FileOutputStream stream = new FileOutputStream(BASE_PATH + File.separatorChar + fileName);
      stream.write(contents, position, size);
      stream.close();
    } catch (NoSuchAlgorithmException | IOException e) {
      System.out.println("Filesystem error - write operation failed" + e.getMessage());
      throw new FileSystemException("Write operation failed");
    }

  }

  public int FS_read(int id, int position, int size, byte[] buffer) {
    // TODO Auto-generated method stub
    return 0;
  }

}
