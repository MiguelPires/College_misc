package pt.tecnico;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoHelper {

    private String algorithm;
    private String transformation;
    private SecretKey lastKey;
    private static final int KEY_SIZE = 128;

    public String getTransformation() {
        return transformation;
    }

    public CryptoHelper(String algorithm, String mode, String padding) {
        this.algorithm = algorithm;
        this.transformation = algorithm + "/" + mode + "/" + padding;
    }

    // returns the last generated key
    public SecretKey getLastKey() {
        return lastKey;
    }

    // ciphers a given byte array with a given key
    public byte[] cipherBytes(byte[] bytes, SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
                                                          IllegalBlockSizeException, BadPaddingException,
                                                          InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(transformation);
        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivspec);
        return cipher.doFinal(bytes);
    }

    // deciphers a given byte array with a given key
    public byte[] decipherBytes(byte[] bytes, SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
                                                            IllegalBlockSizeException, BadPaddingException,
                                                            InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(transformation);
        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivspec);
        return cipher.doFinal(bytes);
    }

    // generates a key of the type specified in the constructor
    public SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
        keyGen.init(KEY_SIZE);
        lastKey = keyGen.generateKey();
        return lastKey;
    }

    // generates a key based on a string
    public SecretKey generateKeyFromPassword(String password, String username) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), generateSalt(username), 1000, KEY_SIZE);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKey key = (PBEKey) factory.generateSecret(keySpec);
        return new SecretKeySpec(key.getEncoded(), algorithm);
    }
    
    // generates a 128-bit salt from a password
    public byte[] generateSalt(String username) throws InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] b = new byte[1];
        b[0] = (byte) 0;
        PBEKeySpec keySpec = new PBEKeySpec(username.toCharArray(), b, 1000, KEY_SIZE);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKey key = (PBEKey) factory.generateSecret(keySpec);
        return key.getEncoded();
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
