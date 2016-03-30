package sec.blockfs.blocktest;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pteidlib.PteidException;
import pteidlib.pteid;
import sec.blockfs.blocklibrary.BlockLibraryImpl;
import sec.blockfs.blockserver.ServerImpl;
import sec.blockfs.blockutility.BlockUtility;
import sec.blockfs.blockutility.DataIntegrityFailureException;
import sec.blockfs.blockutility.OperationFailedException;
import sec.blockfs.blockutility.WrongArgumentsException;
import sun.security.pkcs11.wrapper.PKCS11Constants;
import sun.security.pkcs11.wrapper.PKCS11Exception;

@SuppressWarnings("restriction")
public class LibraryTest {
    private static String servicePort = System.getProperty("service.port");
    private static String serviceName = System.getProperty("service.name");
    private static String serviceUrl = System.getProperty("service.url");
    private static Registry registry;
    private static BlockLibraryImpl library;

    @BeforeClass
    public static void setUpClass() throws NumberFormatException, RemoteException {
        try {
            registry = LocateRegistry.createRegistry(new Integer(servicePort));
            registry.rebind(serviceName, new ServerImpl());
            library = new BlockLibraryImpl(serviceName, servicePort, serviceUrl);
            library.ENABLE_CACHE = false;
            library.FS_init();
        } catch (Exception e) {
            return;
        }
    }

    @AfterClass
    public static void tearDownClass()
            throws AccessException, RemoteException, NotBoundException, MalformedURLException {
        if (registry != null) {
            try {
                Naming.unbind(serviceName);
            } catch (Exception e) {
            }
            try {
                registry.unbind(serviceName);
            } catch (Exception e) {
            }
        }
        
        try {
            library.pkcs11.C_Logout(library.sessionToken);
        } catch (PKCS11Exception e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUp() throws IOException {
        File blockDir = new File(BlockUtility.BASE_PATH);
        if (blockDir.exists())
            FileUtils.cleanDirectory(blockDir);
    }

    @Test
    public void successPerformWrite() throws Exception {
        String text = "Some random write";
        byte[] textBytes = text.getBytes();
        library.FS_write(0, textBytes.length, textBytes);
    }

    @Test
    public void successEmptyWrite() throws Exception {
        byte[] textBytes = "".getBytes();
        library.FS_write(0, textBytes.length, textBytes);
    }

    @Test(expected = WrongArgumentsException.class)
    public void failNullWrite() throws Exception {
        library.FS_write(0, 0, null);
    }

    @Test(expected = WrongArgumentsException.class)
    public void failNegativeSizeArgument() throws Exception {
        library.FS_write(0, -1, "".getBytes());
    }

    @Test(expected = WrongArgumentsException.class)
    public void failNegativeOffsetArgument() throws Exception {
        library.FS_write(-1, 0, "".getBytes());
    }

    @Test(expected = WrongArgumentsException.class)
    public void failSizeArgumentTooBig() throws Exception {
        byte[] data = "abcdefg".getBytes();
        library.FS_write(0, data.length + 1, data);
    }

    /*
     * Data Integrity Checks
     */

    @Test
    public void partialWrite() throws Exception {
        byte[] data = "abcdef".getBytes();
        byte[] halfData = "abc".getBytes();

        // only write half of the data
        library.FS_write(0, halfData.length, data);

        byte[] expectedData = new byte[BlockUtility.BLOCK_SIZE];
        System.arraycopy(halfData, 0, expectedData, 0, halfData.length);

        byte[] buffer = new byte[BlockUtility.BLOCK_SIZE];
        library.FS_read(library.publicKey.getEncoded(), 0, BlockUtility.BLOCK_SIZE, buffer);

        assertTrue("Wrong data", Arrays.equals(expectedData, buffer));
    }

    @Test
    public void publicKeyBlockCheck() throws Exception {
        // write a message
        String text = "Some random content";
        byte[] textBytes = text.getBytes();
        library.FS_write(0, textBytes.length, textBytes);

        // compute hash of public key
        byte[] keyDigest = BlockUtility.digest(library.publicKey.getEncoded());
        String fileName = BlockUtility.getKeyString(keyDigest);

        // check for public key block
        String filePath = BlockUtility.BASE_PATH + File.separatorChar + fileName;
        File file = new File(filePath);
        assertTrue("Public key block '" + filePath + "' doesn't exist", file.exists());
    }

    @Test
    public void dataBlockCheck() throws Exception {
        // write a message
        String text = "Some random content";
        byte[] textBytes = text.getBytes();
        library.FS_write(0, textBytes.length, textBytes);

        byte[] data = new byte[BlockUtility.BLOCK_SIZE];
        System.arraycopy(textBytes, 0, data, 0, textBytes.length);

        // compute hash of data
        byte[] dataDigest = BlockUtility.digest(data);
        String fileName = BlockUtility.getKeyString(dataDigest);

        // check for data block
        String filePath = BlockUtility.BASE_PATH + File.separatorChar + fileName;
        File file = new File(filePath);
        assertTrue("Data block '" + filePath + "' doesn't exist", file.exists());
    }

    @Test
    public void publicKeyBlockContentsCheck() throws Exception {
        // write a message
        String text = "Some random content";
        byte[] textBytes = text.getBytes();
        library.FS_write(0, textBytes.length, textBytes);

        byte[] buffer = new byte[textBytes.length];
        int bytesRead =
            library.FS_read(library.publicKey.getEncoded(), 0, textBytes.length, buffer);
        assertTrue("Read returned wrong data: " + Arrays.toString(buffer) + "; Expected: "
                + Arrays.toString(textBytes), Arrays.equals(buffer, textBytes));
        assertTrue("Read wrong ammount of data. Should've read " + textBytes.length
                + " bytes instead of " + bytesRead, bytesRead == textBytes.length);

        // compute hash of public key
        byte[] keyDigest = BlockUtility.digest(library.publicKey.getEncoded());
        String fileName = BlockUtility.getKeyString(keyDigest);
        String filePath = BlockUtility.BASE_PATH + File.separatorChar + fileName;
        FileInputStream stream = new FileInputStream(filePath);
        byte[] publicBlock = new byte[BlockUtility.SIGNATURE_SIZE + BlockUtility.DIGEST_SIZE];
        stream.read(publicBlock);
        stream.close();

        // extract data
        byte[] publicKeyData = new byte[BlockUtility.DIGEST_SIZE];
        System.arraycopy(publicBlock, BlockUtility.SIGNATURE_SIZE, publicKeyData, 0,
                publicKeyData.length);
        byte[] dataBlock = new byte[BlockUtility.BLOCK_SIZE];
        System.arraycopy(textBytes, 0, dataBlock, 0, textBytes.length);
        assertTrue("Public key block contains wrong data",
                Arrays.equals(publicKeyData, BlockUtility.digest(dataBlock)));
    }

    @Test
    public void dataBlockContentsCheck() throws Exception {
        // write a message
        String text = "Some random content";
        byte[] textBytes = text.getBytes();
        library.FS_write(0, textBytes.length, textBytes);

        // hash of data - expected contents
        byte[] data = new byte[BlockUtility.BLOCK_SIZE];
        System.arraycopy(textBytes, 0, data, 0, textBytes.length);
        String fileName = BlockUtility.getKeyString(BlockUtility.digest(data));
        String filePath = BlockUtility.BASE_PATH + File.separatorChar + fileName;

        // verify data block contents
        FileInputStream stream = new FileInputStream(filePath);
        byte[] buffer = new byte[textBytes.length];
        stream.read(buffer, 0, textBytes.length);
        stream.close();
        assertTrue("Data block contains wrong data", Arrays.equals(buffer, textBytes));
    }

    @Test
    public void twoBlocksDataCheck() throws Exception {
        String text = "Start_" + BlockUtility.generateString(BlockUtility.BLOCK_SIZE) + "_End";
        byte[] textBytes = text.getBytes();
        // System.out.println("Expected: " + Arrays.toString(textBytes));

        library.FS_write(0, textBytes.length, textBytes);

        byte[] readBytes = new byte[textBytes.length];
        int bytesRead =
            library.FS_read(library.publicKey.getEncoded(), 0, textBytes.length, readBytes);

        assertTrue("The written and read bytes don't match", Arrays.equals(textBytes, readBytes));
        assertTrue("Read wrong ammount of data. Should've read " + textBytes.length
                + " bytes instead of " + bytesRead, bytesRead == textBytes.length);

        byte[] firstBlock = new byte[BlockUtility.BLOCK_SIZE];
        System.arraycopy(textBytes, 0, firstBlock, 0, BlockUtility.BLOCK_SIZE);
        byte[] secondBlock = new byte[BlockUtility.BLOCK_SIZE];
        System.arraycopy(textBytes, BlockUtility.BLOCK_SIZE, secondBlock, 0,
                textBytes.length - BlockUtility.BLOCK_SIZE);

        String firstBlockName = BlockUtility.getKeyString(BlockUtility.digest(firstBlock));
        String secondBlockName = BlockUtility.getKeyString(BlockUtility.digest(secondBlock));

        // verify the first block's data
        String filePath = BlockUtility.BASE_PATH + File.separatorChar + firstBlockName;
        FileInputStream stream = new FileInputStream(filePath);
        byte[] buffer = new byte[BlockUtility.BLOCK_SIZE];
        stream.read(buffer, 0, BlockUtility.BLOCK_SIZE);
        stream.close();

        assertTrue("First block contains wrong data", Arrays.equals(buffer, firstBlock));

        // verify the second block's data
        filePath = BlockUtility.BASE_PATH + File.separatorChar + secondBlockName;
        stream = new FileInputStream(filePath);
        buffer = new byte[BlockUtility.BLOCK_SIZE];
        stream.read(buffer, 0, textBytes.length - BlockUtility.BLOCK_SIZE);
        stream.close();

        assertTrue("Second block contains wrong data", Arrays.equals(buffer, secondBlock));
    }

    @Test
    public void twoBlocksPublicCheck() throws Exception {
        String text = "Start_" + BlockUtility.generateString(BlockUtility.BLOCK_SIZE) + "_End";
        byte[] textBytes = text.getBytes();
        // System.out.println("Expected: " + Arrays.toString(textBytes));

        library.FS_write(0, textBytes.length, textBytes);

        // get public key block
        String fileName =
            BlockUtility.getKeyString(BlockUtility.digest(library.publicKey.getEncoded()));
        String filePath = BlockUtility.BASE_PATH + File.separatorChar + fileName;
        FileInputStream stream = new FileInputStream(filePath);
        byte[] publicKeyBlock =
            new byte[BlockUtility.SIGNATURE_SIZE + BlockUtility.DIGEST_SIZE * 2];
        stream.read(publicKeyBlock, 0, publicKeyBlock.length);
        stream.close();

        // compute data block hashes
        byte[] firstBlock = new byte[BlockUtility.BLOCK_SIZE];
        System.arraycopy(textBytes, 0, firstBlock, 0, BlockUtility.BLOCK_SIZE);
        byte[] secondBlock = new byte[BlockUtility.BLOCK_SIZE];
        System.arraycopy(textBytes, BlockUtility.BLOCK_SIZE, secondBlock, 0,
                textBytes.length - BlockUtility.BLOCK_SIZE);

        byte[] firstBlockHash = BlockUtility.digest(firstBlock);
        byte[] secondBlockHash = BlockUtility.digest(secondBlock);

        byte[] blockHashes = new byte[BlockUtility.DIGEST_SIZE * 2];
        System.arraycopy(firstBlockHash, 0, blockHashes, 0, BlockUtility.DIGEST_SIZE);
        System.arraycopy(secondBlockHash, 0, blockHashes, BlockUtility.DIGEST_SIZE,
                BlockUtility.DIGEST_SIZE);

        // create signature
        long sessionToken =
            library.pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);
        library.pkcs11.C_SignInit(sessionToken, library.mechanism, library.privateKey);
        byte[] keyBlockSignature = library.pkcs11.C_Sign(sessionToken, blockHashes);
        library.pkcs11.C_CloseSession(library.privateKey);

        /*
         * Signature signAlgorithm = Signature.getInstance("SHA512withRSA", "SunRsaSign");
         * signAlgorithm.initSign(library.privateKey); signAlgorithm.update(blockHashes, 0, blockHashes.length); byte[]
         * keyBlockSignature = signAlgorithm.sign();
         */

        byte[] storedSignature = new byte[BlockUtility.SIGNATURE_SIZE];
        System.arraycopy(publicKeyBlock, 0, storedSignature, 0, BlockUtility.SIGNATURE_SIZE);
        assertTrue("The public block's signature is wrong",
                Arrays.equals(keyBlockSignature, storedSignature));

        byte[] storedFirstHash = new byte[BlockUtility.DIGEST_SIZE];
        System.arraycopy(publicKeyBlock, BlockUtility.SIGNATURE_SIZE, storedFirstHash, 0,
                BlockUtility.DIGEST_SIZE);
        assertTrue("The first block's hash is wrong",
                Arrays.equals(storedFirstHash, firstBlockHash));

        byte[] storedSecondHash = new byte[BlockUtility.DIGEST_SIZE];
        System.arraycopy(publicKeyBlock, BlockUtility.SIGNATURE_SIZE + BlockUtility.DIGEST_SIZE,
                storedSecondHash, 0, BlockUtility.DIGEST_SIZE);
        assertTrue("The second block's hash is wrong",
                Arrays.equals(storedSecondHash, secondBlockHash));
    }

    @Test(expected = DataIntegrityFailureException.class)
    public void addDataBlockAttack() throws Exception {
        String text = BlockUtility.generateString(BlockUtility.BLOCK_SIZE);
        byte[] textBytes = text.getBytes();

        library.FS_write(0, textBytes.length, textBytes);

        // get public key block
        String fileName =
            BlockUtility.getKeyString(BlockUtility.digest(library.publicKey.getEncoded()));
        String filePath = BlockUtility.BASE_PATH + File.separatorChar + fileName;
        FileInputStream stream = new FileInputStream(filePath);
        byte[] publicKeyBlock = new byte[BlockUtility.SIGNATURE_SIZE + BlockUtility.DIGEST_SIZE];
        stream.read(publicKeyBlock, 0, publicKeyBlock.length);
        stream.close();

        byte[] alteredTextBytes = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        byte[] alteredHash = BlockUtility.digest(alteredTextBytes);

        String newBlockName = BlockUtility.getKeyString(alteredHash);
        String newBlockPath = BlockUtility.BASE_PATH + File.separatorChar + newBlockName;

        FileOutputStream outStream = new FileOutputStream(newBlockPath);
        outStream.write(alteredTextBytes);
        outStream.close();

        // rewrite public key block to include new data block
        byte[] rewrittenPublicKeyBlock =
            new byte[BlockUtility.SIGNATURE_SIZE + BlockUtility.DIGEST_SIZE * 2];
        System.arraycopy(publicKeyBlock, 0, rewrittenPublicKeyBlock, 0,
                BlockUtility.SIGNATURE_SIZE + BlockUtility.DIGEST_SIZE);
        System.arraycopy(alteredHash, 0, rewrittenPublicKeyBlock,
                BlockUtility.SIGNATURE_SIZE + BlockUtility.DIGEST_SIZE, BlockUtility.DIGEST_SIZE);
        outStream = new FileOutputStream(filePath);
        outStream.write(rewrittenPublicKeyBlock);
        outStream.close();

        byte[] readBuffer = new byte[BlockUtility.BLOCK_SIZE];
        library.FS_read(library.publicKey.getEncoded(), 0, BlockUtility.BLOCK_SIZE, readBuffer);

    }

    @Test(expected = DataIntegrityFailureException.class)
    public void changeDataBlockAttack() throws Exception {
        String text = BlockUtility.generateString(BlockUtility.BLOCK_SIZE);
        byte[] textBytes = text.getBytes();

        library.FS_write(0, textBytes.length, textBytes);

        // get data block
        String fileName = BlockUtility.getKeyString(BlockUtility.digest(textBytes));
        String filePath = BlockUtility.BASE_PATH + File.separatorChar + fileName;
        FileInputStream stream = new FileInputStream(filePath);
        byte[] dataBlock = new byte[BlockUtility.BLOCK_SIZE];
        stream.read(dataBlock, 0, dataBlock.length);
        stream.close();

        byte[] alteration = "altered".getBytes();
        System.arraycopy(alteration, 0, dataBlock, 0, alteration.length);

        FileOutputStream outStream = new FileOutputStream(filePath);
        outStream.write(dataBlock);
        outStream.close();

        byte[] readBuffer = new byte[BlockUtility.BLOCK_SIZE];
        library.FS_read(library.publicKey.getEncoded(), 0, BlockUtility.BLOCK_SIZE, readBuffer);
    }

    @Test(expected = DataIntegrityFailureException.class)
    public void changePublicBlockAttack() throws Exception {
        String text = BlockUtility.generateString(BlockUtility.BLOCK_SIZE);
        byte[] textBytes = text.getBytes();
        library.FS_write(0, textBytes.length, textBytes);

        // get public key block
        String fileName =
            BlockUtility.getKeyString(BlockUtility.digest(library.publicKey.getEncoded()));
        String filePath = BlockUtility.BASE_PATH + File.separatorChar + fileName;
        FileInputStream stream = new FileInputStream(filePath);
        byte[] publicKeyBlock = new byte[BlockUtility.SIGNATURE_SIZE + BlockUtility.DIGEST_SIZE];
        stream.read(publicKeyBlock, 0, publicKeyBlock.length);
        stream.close();

        byte[] alteredTextBytes = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        byte[] alteredHash = BlockUtility.digest(alteredTextBytes);

        // write new block
        String newBlockName = BlockUtility.getKeyString(alteredHash);
        String newBlockPath = BlockUtility.BASE_PATH + File.separatorChar + newBlockName;
        FileOutputStream outStream = new FileOutputStream(newBlockPath);
        outStream.write(alteredTextBytes);
        outStream.close();

        // rewrite public key block to ignore the previous block and point to a new one
        byte[] rewrittenPublicKeyBlock =
            new byte[BlockUtility.SIGNATURE_SIZE + BlockUtility.DIGEST_SIZE];
        System.arraycopy(publicKeyBlock, 0, rewrittenPublicKeyBlock, 0,
                BlockUtility.SIGNATURE_SIZE);
        System.arraycopy(alteredHash, 0, rewrittenPublicKeyBlock, BlockUtility.SIGNATURE_SIZE,
                BlockUtility.DIGEST_SIZE);
        outStream = new FileOutputStream(filePath);
        outStream.write(rewrittenPublicKeyBlock);
        outStream.close();

        byte[] readBuffer = new byte[BlockUtility.BLOCK_SIZE];
        library.FS_read(library.publicKey.getEncoded(), 0, BlockUtility.BLOCK_SIZE, readBuffer);
    }

    @Test(expected = OperationFailedException.class)
    public void deleteDataBlockAttack() throws Exception {
        String text = BlockUtility.generateString(BlockUtility.BLOCK_SIZE);
        byte[] textBytes = text.getBytes();
        library.FS_write(0, textBytes.length, textBytes);

        // delete data block
        String fileName = BlockUtility.getKeyString(BlockUtility.digest(textBytes));
        String filePath = BlockUtility.BASE_PATH + File.separatorChar + fileName;
        System.out.println("Deleting file " + fileName);
        File dataBlock = new File(filePath);

        assertTrue("The test couldn't delete the file - invalid", dataBlock.delete());

        byte[] readBuffer = new byte[BlockUtility.BLOCK_SIZE];
        library.FS_read(library.publicKey.getEncoded(), 0, BlockUtility.BLOCK_SIZE, readBuffer);
    }

    /*
     * SmartCard Authentication Tests
     */

    @Test
    public void successReadPublicKey()
            throws OperationFailedException, PteidException, CertificateException {
        List<X509Certificate> readCertificates = library.FS_list();

        assertTrue("There are no stored certificates", readCertificates != null);
        assertTrue("Expected one certificate. " + readCertificates.size() + " certs read instead",
                readCertificates.size() == 1);

        byte[] byteCert = pteid.GetCertificates()[0].certif;
        X509Certificate clientCert = BlockUtility.getCertFromByteArray(byteCert);
        X509Certificate storedCert = readCertificates.get(0);

        assertTrue("The stored cert isn't equal to the client cert", storedCert.equals(clientCert));
    }

    /*
     * @Test(expected = DataIntegrityFailureException.class) public void keyListingChangeAttack() throws OperationFailedException,
     * PteidException, CertificateException { // obtain the client certificate byte[] authCertBytes =
     * BlockUtility.getCertificateInBytes(1); X509Certificate authCert = BlockUtility.getCertFromByteArray(authCertBytes);
     * 
     * // build cert chain ArrayList<X509Certificate> certs = new ArrayList<X509Certificate>(); certs.add(authCert);
     * certs.add(BlockUtility.getCertFromByteArray(BlockUtility.getCertificateInBytes(3)));
     * certs.add(BlockUtility.getCertFromByteArray(BlockUtility.getCertificateInBytes(4)));
     * certs.add(BlockUtility.getCertFromByteArray(BlockUtility.getCertificateInBytes(7)));
     * 
     * CertificateFactory fact = CertificateFactory.getInstance("X.509"); CertPath path = fact.generateCertPath(certs);
     * 
     * // add corrupt chain ServerImpl server = (ServerImpl) library.blockServer; server.certificates.add(path);
     * 
     * library.FS_list(); }
     */
}
