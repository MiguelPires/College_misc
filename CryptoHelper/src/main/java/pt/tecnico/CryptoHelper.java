package pt.tecnico;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
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

    // ciphers or deciphers a given byte array with a given key
    public byte[] cipherBytes(byte[] bytes, SecretKey key) throws NoSuchAlgorithmException,
                                                          NoSuchPaddingException,
                                                          InvalidKeyException,
                                                          IllegalBlockSizeException,
                                                          BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(transformation);
        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivspec);
        return cipher.doFinal(bytes);
    }

    public byte[] decipherBytes(byte[] bytes, SecretKey key) throws NoSuchAlgorithmException,
                                                          NoSuchPaddingException,
                                                          InvalidKeyException,
                                                          IllegalBlockSizeException,
                                                          BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(transformation);
        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivspec);
        return cipher.doFinal(bytes);
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
