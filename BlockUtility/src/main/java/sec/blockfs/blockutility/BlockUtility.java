package sec.blockfs.blockutility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BlockUtility {
    // cross-module variables
    public static final int KEY_SIZE = 2048;
    // TODO: adicionar os algoritmos e providers (RSA, SUN, etc)
    public static final String DIGEST_ALGORITHM = "SHA-512"; 
    // utility methods
    public static String getKeyString(byte[] contents) {
        String keyString = "";
        int stringSize = 10;
        for (int i = 0; i < contents.length && i < stringSize; ++i) {
            keyString += contents[i];
        }
        return keyString;
    }

    public static byte[] digest(byte[] data) {
        MessageDigest digestAlgorithm = null;
        try {
            digestAlgorithm = MessageDigest.getInstance(DIGEST_ALGORITHM);
            digestAlgorithm.reset();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return digestAlgorithm.digest(data);
    }
}
