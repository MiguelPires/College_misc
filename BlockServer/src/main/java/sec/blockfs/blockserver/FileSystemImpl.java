package sec.blockfs.blockserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystemException;

import sec.blockfs.blockutility.BlockUtility;

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
      byte[] dataDigest = BlockUtility.digest(contents);
      String fileName = BlockUtility.getKeyString(dataDigest);
      String filePath = BASE_PATH + File.separatorChar + fileName;

      System.out.println("Writing data block: " + filePath);
      FileOutputStream stream = new FileOutputStream(filePath);
      stream.write(contents, position, size);
      stream.close();
    } catch (IOException e) {
      System.out.println("Filesystem error - write operation failed" + e.getMessage());
      throw new FileSystemException("Write operation failed");
    }

  }

  public int FS_read(int id, int position, int size, byte[] buffer) {
    // TODO Auto-generated method stub
    return 0;
  }

}
