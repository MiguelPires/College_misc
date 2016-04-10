package sec.blockfs.blockserver;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import sec.blockfs.blockutility.BlockUtility;

public class ServerTest {
    private static String servicePort = System.getProperty("service.port");
    private static String serviceName = System.getProperty("service.name");
    private static String serviceUrl = System.getProperty("service.url");
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private Signature signAlgorithm;

    @Before
    public void setUp() throws Exception {
        // instantiate key generator
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "SunRsaSign");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(BlockUtility.KEY_SIZE, random);

        // generate keys
        KeyPair pair = keyGen.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();

        signAlgorithm = Signature.getInstance("SHA512withRSA", "SunRsaSign");
    }

    @Test
    public void successCreateService() throws Exception {
        Registry registry = LocateRegistry.createRegistry(new Integer(servicePort));
        registry.rebind(serviceName, new ServerImpl());
    }

    @Test
    public void sucessPut_h() throws Exception {
        BlockServer server = new ServerImpl();
        byte[] data = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        server.put_h(data);

        String fileName = BlockUtility.getKeyString(BlockUtility.digest(data));
        String filePath = FileSystemImpl.BASE_PATH + "-" + 0 + File.separatorChar + fileName;
        FileInputStream inputStream = new FileInputStream(filePath);
        byte[] buffer = new byte[data.length];
        inputStream.read(buffer);
        inputStream.close();

        assertTrue("Stored data block is different than expected", Arrays.equals(data, buffer));
    }

    @Test
    public void sucessPut_k() throws Exception {
        BlockServer server = new ServerImpl();
        byte[] data = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        byte[] dataHash = BlockUtility.digest(data);

        signAlgorithm.initSign(privateKey);
        signAlgorithm.update(dataHash);
        byte[] signature = signAlgorithm.sign();

        server.put_k(dataHash, signature, publicKey.getEncoded());

        String fileName = BlockUtility.getKeyString(BlockUtility.digest(publicKey.getEncoded()));
        String filePath = FileSystemImpl.BASE_PATH + "-" + 0 + File.separatorChar + fileName;
        FileInputStream inputStream = new FileInputStream(filePath);

        byte[] buffer = new byte[BlockUtility.DIGEST_SIZE + BlockUtility.SIGNATURE_SIZE];
        inputStream.read(buffer);
        inputStream.close();

        byte[] storedSignature = new byte[BlockUtility.SIGNATURE_SIZE];
        System.arraycopy(buffer, 0, storedSignature, 0, BlockUtility.SIGNATURE_SIZE);
        byte[] storedData = new byte[BlockUtility.DIGEST_SIZE];
        System.arraycopy(buffer, BlockUtility.SIGNATURE_SIZE, storedData, 0, BlockUtility.DIGEST_SIZE);

        // verify public key block integrity
        boolean verified = BlockUtility.verifyDataIntegrity(storedData, storedSignature, publicKey);

        assertTrue("The signature is different from expected", Arrays.equals(storedSignature, signature));
        assertTrue("The stored signature is incorrect", verified);
        assertTrue("Stored public key block is different than expected", Arrays.equals(dataHash, storedData));
    }

    @Test
    public void sucessGet() throws Exception {
        byte[] data = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        byte[] dataDigest = BlockUtility.digest(data);

        String fileName = BlockUtility.getKeyString(dataDigest);
        String filePath = FileSystemImpl.BASE_PATH + "-" + 0 + File.separatorChar + fileName;
        FileOutputStream outStream = new FileOutputStream(filePath);
        outStream.write(data);
        outStream.close();

        BlockServer server = new ServerImpl();
        byte[] storedBlock = server.get(fileName);
        byte[] storedHash = BlockUtility.digest(storedBlock);

        assertTrue("Retrieved data different from expected", Arrays.equals(data, storedBlock));
        assertTrue("Retrieved data different from expected", fileName.equals(BlockUtility.getKeyString(storedHash)));
    }

    @Test(expected = FileNotFoundException.class)
    public void wrongBlockGet() throws Exception {
        String fileName = BlockUtility.generateString(10);
        BlockServer server = new ServerImpl();
        server.get(fileName);
    }

    @Test(expected = ServerErrorException.class)
    public void invalidDataPut_k() throws Exception {
        BlockServer server = new ServerImpl();
        byte[] data = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        byte[] dataHash = BlockUtility.digest(data);

        signAlgorithm.initSign(privateKey);
        signAlgorithm.update(dataHash);
        byte[] signature = signAlgorithm.sign();

        server.put_k(null, signature, publicKey.getEncoded());
    }

    @Test(expected = DataIntegrityFailureException.class)
    public void wrongDataPut_k() throws Exception {
        BlockServer server = new ServerImpl();
        byte[] data = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        byte[] dataHash = BlockUtility.digest(data);

        signAlgorithm.initSign(privateKey);
        signAlgorithm.update(dataHash);
        byte[] signature = signAlgorithm.sign();

        server.put_k(data, signature, publicKey.getEncoded());
    }

    @Test(expected = ServerErrorException.class)
    public void invalidSignaturePut_k() throws Exception {
        BlockServer server = new ServerImpl();
        byte[] data = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        byte[] dataHash = BlockUtility.digest(data);

        signAlgorithm.initSign(privateKey);
        signAlgorithm.update(dataHash);
        byte[] signature = signAlgorithm.sign();

        server.put_k(dataHash, null, publicKey.getEncoded());
    }

    @Test(expected = DataIntegrityFailureException.class)
    public void wrongSignaturePut_k() throws Exception {
        BlockServer server = new ServerImpl();
        byte[] data = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        byte[] dataHash = BlockUtility.digest(data);

        signAlgorithm.initSign(privateKey);
        signAlgorithm.update(data);
        byte[] signature = signAlgorithm.sign();

        server.put_k(dataHash, signature, publicKey.getEncoded());
    }

    @Test(expected = ServerErrorException.class)
    public void invalidPublicKeyPut_k() throws Exception {
        BlockServer server = new ServerImpl();
        byte[] data = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        byte[] dataHash = BlockUtility.digest(data);

        signAlgorithm.initSign(privateKey);
        signAlgorithm.update(data);
        byte[] signature = signAlgorithm.sign();

        server.put_k(dataHash, signature, null);
    }

    @Test(expected = DataIntegrityFailureException.class)
    public void wrongPublicKeyPut_k() throws Exception {
        BlockServer server = new ServerImpl();
        byte[] data = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        byte[] dataHash = BlockUtility.digest(data);

        signAlgorithm.initSign(privateKey);
        signAlgorithm.update(data);
        byte[] signature = signAlgorithm.sign();

        // instantiate key generator
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "SunRsaSign");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(BlockUtility.KEY_SIZE, random);

        // generate keys
        KeyPair pair = keyGen.generateKeyPair();
        PublicKey publicKey = pair.getPublic();

        server.put_k(dataHash, signature, publicKey.getEncoded());
    }

    @Test(expected = ServerErrorException.class)
    public void invalidPut_h() throws Exception {
        BlockServer server = new ServerImpl();
        server.put_h(null);
    }
}
