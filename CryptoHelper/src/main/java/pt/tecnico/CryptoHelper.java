package pt.tecnico;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CryptoHelper {

    private String algorithm;
    private String mode;
    private String padding;
    private String transformation;
    private SecretKey lastKey;

    public String getTransformation() {
        return transformation;
    }
    
    public CryptoHelper(String algorithm) {
        this.algorithm = algorithm;
        this.mode = "CBC";
        this.padding = "PKCS5Padding";
        this.transformation = algorithm + "/" + mode + "/" + padding;
    }

    public CryptoHelper(String algorithm, String mode, String padding) {
        this.algorithm = algorithm;
        this.mode = mode;
        this.padding = padding;
        this.transformation = algorithm + "/" + mode + "/" + padding;
    }

    // returns the last generated key
    public SecretKey getLastKey() {
        return lastKey;
    }

    // cyphers a given byte array with a given key
    public byte[] cypherBytes(byte[] plain, SecretKey key) throws NoSuchAlgorithmException,
                                                          NoSuchPaddingException,
                                                          InvalidKeyException,
                                                          IllegalBlockSizeException,
                                                          BadPaddingException {
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(plain);
    }

    // generates key of the type specified in the constructor
    public SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
        keyGen.init(128);
        lastKey = keyGen.generateKey();
        return lastKey;
    }

    // encodes key to base 64
    public String encodeKey(SecretKey key) throws NoSuchAlgorithmException {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    // decodes key from base 64
    public SecretKey decodeKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, algorithm);
    }
}
