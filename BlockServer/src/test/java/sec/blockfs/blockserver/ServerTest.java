package sec.blockfs.blockserver;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import pteidlib.pteid;
import sec.blockfs.blockutility.BlockUtility;
import sun.security.pkcs11.wrapper.CK_ATTRIBUTE;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_MECHANISM;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Constants;

@SuppressWarnings("restriction")
public class ServerTest {
    private static String servicePort = System.getProperty("service.port");
    private static String serviceName = System.getProperty("service.name");
    private static String serviceUrl = System.getProperty("service.url");
    
    private static Signature signAlgorithm;
    private static PKCS11 pkcs11;
    private static PublicKey publicKey;
    private static long privateKey;
    private static CK_MECHANISM mechanism; // access mechanism
    private static  long sessionToken;
    
    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void setUp() throws Exception {
        System.loadLibrary("pteidlibj");
        pteid.Init(""); // Initializes the eID Lib
        pteid.SetSODChecking(false);

        String osName = System.getProperty("os.name");
        String javaVersion = System.getProperty("java.version");
        String libName = "libpteidpkcs11.so";

        if (-1 != osName.indexOf("Windows"))
            libName = "pteidpkcs11.dll";
        else if (-1 != osName.indexOf("Mac"))
            libName = "pteidpkcs11.dylib";

        Class pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
        if (javaVersion.startsWith("1.5.")) {
            Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance",
                    new Class[] { String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
            pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[] { libName, null, false });
        } else {
            Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance",
                    new Class[] { String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
            pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[] { libName, "C_GetFunctionList", null, false });
        }

        // Open the PKCS11 session
        System.out.println("            //Open the PKCS11 session");
        sessionToken = pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);
        
        // Token login
        System.out.println("            //Token login");
        pkcs11.C_Login(sessionToken, 1, null);
        // CK_SESSION_INFO info = pkcs11.C_GetSessionInfo(sessionToken);

        System.out.println("            //Get available keys");
        CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
        attributes[0] = new CK_ATTRIBUTE();
        attributes[0].type = PKCS11Constants.CKA_CLASS;
        attributes[0].pValue = new Long(PKCS11Constants.CKO_PRIVATE_KEY);

        pkcs11.C_FindObjectsInit(sessionToken, attributes);
        long[] keyHandles = pkcs11.C_FindObjects(sessionToken, 5);

        // points to auth_key
        System.out.println("            //points to auth_key. No. of keys:" + keyHandles.length);

        privateKey = keyHandles[0]; // test with other keys to see
                                    // what you get
        pkcs11.C_FindObjectsFinal(sessionToken);

        // initialize the signature method
        System.out.println("            //initialize the signature method");
        mechanism = new CK_MECHANISM();
        mechanism.mechanism = PKCS11Constants.CKM_SHA1_RSA_PKCS;
        mechanism.pParameter = null;
        signAlgorithm = Signature.getInstance("SHA512withRSA", "SunRsaSign");
        
        byte[] authCertBytes = BlockUtility.getCertificateInBytes(0);
        X509Certificate authCert = BlockUtility.getCertFromByteArray(authCertBytes);
        publicKey = authCert.getPublicKey();
    }
    
    @AfterClass
    public static void tearDown() {
        try {
            pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD);
            pkcs11.C_CloseSession(sessionToken);
        } catch (Exception e) {
            ;
        }
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
        String filePath = FileSystemImpl.BASE_PATH + File.separatorChar + fileName;
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

        pkcs11.C_SignInit(sessionToken, mechanism, privateKey);
        byte[] signature = pkcs11.C_Sign(sessionToken, dataHash);

        server.put_k(dataHash, signature, publicKey.getEncoded());

        String fileName = BlockUtility.getKeyString(BlockUtility.digest(publicKey.getEncoded()));
        String filePath = FileSystemImpl.BASE_PATH + File.separatorChar + fileName;
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
        String filePath = FileSystemImpl.BASE_PATH + File.separatorChar + fileName;
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

        pkcs11.C_SignInit(sessionToken, mechanism, privateKey);
        byte[] signature = pkcs11.C_Sign(sessionToken, dataHash);

        server.put_k(null, signature, publicKey.getEncoded());
    }

    @Test(expected = DataIntegrityFailureException.class)
    public void wrongDataPut_k() throws Exception {
        BlockServer server = new ServerImpl();
        byte[] data = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        byte[] dataHash = BlockUtility.digest(data);


        pkcs11.C_SignInit(sessionToken, mechanism, privateKey);
        byte[] signature = pkcs11.C_Sign(sessionToken, dataHash);

        server.put_k(data, signature, publicKey.getEncoded());
    }

    @Test(expected = ServerErrorException.class)
    public void invalidSignaturePut_k() throws Exception {
        BlockServer server = new ServerImpl();
        byte[] data = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        byte[] dataHash = BlockUtility.digest(data);

        server.put_k(dataHash, null, publicKey.getEncoded());
    }

    @Test(expected = DataIntegrityFailureException.class)
    public void wrongSignaturePut_k() throws Exception {
        BlockServer server = new ServerImpl();
        byte[] data = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        byte[] dataHash = BlockUtility.digest(data);

        pkcs11.C_SignInit(sessionToken, mechanism, privateKey);
        byte[] signature = pkcs11.C_Sign(sessionToken, data);

        server.put_k(dataHash, signature, publicKey.getEncoded());
    }

    @Test(expected = ServerErrorException.class)
    public void invalidPublicKeyPut_k() throws Exception {
        BlockServer server = new ServerImpl();
        byte[] data = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        byte[] dataHash = BlockUtility.digest(data);

        pkcs11.C_SignInit(sessionToken, mechanism, privateKey);
        byte[] signature = pkcs11.C_Sign(sessionToken, dataHash);

        server.put_k(dataHash, signature, null);
    }

    @Test(expected = DataIntegrityFailureException.class)
    public void wrongPublicKeyPut_k() throws Exception {
        BlockServer server = new ServerImpl();
        byte[] data = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        byte[] dataHash = BlockUtility.digest(data);

        pkcs11.C_SignInit(sessionToken, mechanism, privateKey);
        byte[] signature = pkcs11.C_Sign(sessionToken, dataHash);

        byte[] differentCertBytes = BlockUtility.getCertificateInBytes(1);
        X509Certificate differentCert = BlockUtility.getCertFromByteArray(differentCertBytes);
        PublicKey diffPublicKey = differentCert.getPublicKey();

        server.put_k(dataHash, signature, diffPublicKey.getEncoded());
    }

    @Test(expected = ServerErrorException.class)
    public void invalidPut_h() throws Exception {
        BlockServer server = new ServerImpl();
        server.put_h(null);
    }
}
