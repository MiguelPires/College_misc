package sec.blockfs.blocklibrary;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Signature;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sec.blockfs.blockserver.DataIntegrityFailureException;
import sec.blockfs.blockserver.FileSystemImpl;
import sec.blockfs.blockserver.ServerImpl;
import sec.blockfs.blockserver.WrongArgumentsException;
import sec.blockfs.blockutility.BlockUtility;
import sec.blockfs.blockutility.OperationFailedException;

public class LibraryTest {
    private static String servicePort = System.getProperty("service.port");
    private static String serviceName = System.getProperty("service.name");
    private static String serviceUrl = System.getProperty("service.url");
    private Registry registry;

    @Before
    public void setUp() throws NumberFormatException, RemoteException {
        BlockUtility.NUM_REPLICAS = 1;
        BlockUtility.NUM_FAULTS = 0;
        
        try {
            registry = LocateRegistry.createRegistry(new Integer(servicePort));
            registry.rebind(serviceName + "0", new ServerImpl());
        } catch (Exception e) {
            return;
        }
    }

    @After
    public void tearDown() throws AccessException, RemoteException, NotBoundException, MalformedURLException {
        if (registry != null) {
            try {
                Naming.unbind(serviceName);
            } catch (Exception e) {
                return;
            }
            try {

                registry.unbind(serviceName);
            } catch (Exception e) {
                return;
            }
        }
    }

    @Test
    public void successCreateLib() throws Exception {
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();
    }

    @Test(expected = InitializationFailureException.class)
    public void wrongPort() throws Exception {
        BlockLibrary library = new BlockLibrary(serviceName, servicePort + 1, serviceUrl);
        library.FS_init();
    }

    @Test(expected = InitializationFailureException.class)
    public void wrongName() throws Exception {
        BlockLibrary library = new BlockLibrary(serviceName, servicePort + 1, serviceUrl);
        library.FS_init();
    }

    @Test
    public void successPerformWrite() throws Exception {
        String text = "Some random write";
        byte[] textBytes = text.getBytes();
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();
        library.FS_write(0, textBytes.length, textBytes);
    }

    @Test
    public void successEmptyWrite() throws Exception {
        byte[] textBytes = "".getBytes();
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();
        library.FS_write(0, textBytes.length, textBytes);
    }

    @Test(expected = WrongArgumentsException.class)
    public void failNullWrite() throws Exception {
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();
        library.FS_write(0, 0, null);
    }

    @Test(expected = WrongArgumentsException.class)
    public void failNegativeSizeArgument() throws Exception {
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();
        library.FS_write(0, -1, "".getBytes());
    }

    @Test(expected = WrongArgumentsException.class)
    public void failNegativeOffsetArgument() throws Exception {
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();
        library.FS_write(-1, 0, "".getBytes());
    }

    @Test(expected = WrongArgumentsException.class)
    public void failSizeArgumentTooBig() throws Exception {
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();
        byte[] data = "abcdefg".getBytes();
        library.FS_write(0, data.length + 1, data);
    }

    @Test
    public void partialWrite() throws Exception {
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();
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

    @Test(expected = OperationFailedException.class)
    public void readOnly() throws Exception {
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();

        byte[] buffer = new byte[BlockUtility.BLOCK_SIZE];
        library.FS_read(library.publicKey.getEncoded(), 0, BlockUtility.BLOCK_SIZE, buffer);
    }

    @Test
    public void publicKeyBlockCheck() throws Exception {
        // write a message
        String text = "Some random content";
        byte[] textBytes = text.getBytes();
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();
        library.FS_write(0, textBytes.length, textBytes);

        // compute hash of public key
        byte[] keyDigest = BlockUtility.digest(library.publicKey.getEncoded());
        String fileName = BlockUtility.getKeyString(keyDigest);

        // check for public key block
        String filePath = FileSystemImpl.BASE_PATH + "-" + 0 + File.separatorChar + fileName;
        File file = new File(filePath);
        assertTrue("Public key block '" + filePath + "' doesn't exist", file.exists());
    }

    @Test
    public void dataBlockCheck() throws Exception {
        // write a message
        String text = "Some random content";
        byte[] textBytes = text.getBytes();
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();
        library.FS_write(0, textBytes.length, textBytes);

        byte[] data = new byte[BlockUtility.BLOCK_SIZE];
        System.arraycopy(textBytes, 0, data, 0, textBytes.length);

        // compute hash of data
        byte[] dataDigest = BlockUtility.digest(data);
        String fileName = BlockUtility.getKeyString(dataDigest);

        // check for data block
        String filePath = FileSystemImpl.BASE_PATH + "-" + 0 + File.separatorChar + fileName;
        File file = new File(filePath);
        assertTrue("Data block '" + filePath + "' doesn't exist", file.exists());
    }

    @Test
    public void publicKeyBlockContentsCheck() throws Exception {
        // write a message
        String text = "Some random content";
        byte[] textBytes = text.getBytes();
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();
        library.FS_write(0, textBytes.length, textBytes);

        byte[] buffer = new byte[textBytes.length];
        int bytesRead = library.FS_read(library.publicKey.getEncoded(), 0, textBytes.length, buffer);
        assertTrue("Read returned wrong data: " + Arrays.toString(buffer) + "; Expected: " + Arrays.toString(textBytes),
                Arrays.equals(buffer, textBytes));
        assertTrue("Read wrong ammount of data. Should've read " + textBytes.length + " bytes instead of " + bytesRead,
                bytesRead == textBytes.length);

        // compute hash of public key
        byte[] keyDigest = BlockUtility.digest(library.publicKey.getEncoded());
        String fileName = BlockUtility.getKeyString(keyDigest);
        String filePath = FileSystemImpl.BASE_PATH + "-" + 0 + File.separatorChar + fileName;
        FileInputStream stream = new FileInputStream(filePath);
        byte[] publicBlock = new byte[BlockUtility.SIGNATURE_SIZE + 1 + BlockUtility.DIGEST_SIZE];
        stream.read(publicBlock);
        stream.close();

        // extract data
        byte[] publicKeyData = new byte[BlockUtility.DIGEST_SIZE];
        int timestamp = (int) publicBlock[BlockUtility.SIGNATURE_SIZE];
        assertTrue("The timestamp is wrong. Should be one, it's " + timestamp + " instead.", timestamp == 1);

        System.arraycopy(publicBlock, BlockUtility.SIGNATURE_SIZE + 1, publicKeyData, 0, publicKeyData.length);
        byte[] dataBlock = new byte[BlockUtility.BLOCK_SIZE];
        System.arraycopy(textBytes, 0, dataBlock, 0, textBytes.length);
        assertTrue("Public key block contains wrong data", Arrays.equals(publicKeyData, BlockUtility.digest(dataBlock)));
    }

    @Test
    public void dataBlockContentsCheck() throws Exception {
        // write a message
        String text = "Some random content";
        byte[] textBytes = text.getBytes();
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();
        library.FS_write(0, textBytes.length, textBytes);

        // hash of data - expected contents
        byte[] data = new byte[BlockUtility.BLOCK_SIZE];
        System.arraycopy(textBytes, 0, data, 0, textBytes.length);
        String fileName = BlockUtility.getKeyString(BlockUtility.digest(data));
        String filePath = FileSystemImpl.BASE_PATH + "-" + 0 + File.separatorChar + fileName;

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

        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();
        library.FS_write(0, textBytes.length, textBytes);

        byte[] readBytes = new byte[textBytes.length];
        int bytesRead = library.FS_read(library.publicKey.getEncoded(), 0, textBytes.length, readBytes);

        assertTrue("The written and read bytes don't match", Arrays.equals(textBytes, readBytes));
        assertTrue("Read wrong ammount of data. Should've read " + textBytes.length + " bytes instead of " + bytesRead,
                bytesRead == textBytes.length);

        byte[] firstBlock = new byte[BlockUtility.BLOCK_SIZE];
        System.arraycopy(textBytes, 0, firstBlock, 0, BlockUtility.BLOCK_SIZE);
        byte[] secondBlock = new byte[BlockUtility.BLOCK_SIZE];
        System.arraycopy(textBytes, BlockUtility.BLOCK_SIZE, secondBlock, 0, textBytes.length - BlockUtility.BLOCK_SIZE);

        String firstBlockName = BlockUtility.getKeyString(BlockUtility.digest(firstBlock));
        String secondBlockName = BlockUtility.getKeyString(BlockUtility.digest(secondBlock));

        // verify the first block's data
        String filePath = FileSystemImpl.BASE_PATH + "-" + 0 + File.separatorChar + firstBlockName;
        FileInputStream stream = new FileInputStream(filePath);
        byte[] buffer = new byte[BlockUtility.BLOCK_SIZE];
        stream.read(buffer, 0, BlockUtility.BLOCK_SIZE);
        stream.close();

        assertTrue("First block contains wrong data", Arrays.equals(buffer, firstBlock));

        // verify the second block's data
        filePath = FileSystemImpl.BASE_PATH + "-" + 0 + File.separatorChar + secondBlockName;
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

        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();
        library.FS_write(0, textBytes.length, textBytes);

        // get public key block
        String fileName = BlockUtility.getKeyString(BlockUtility.digest(library.publicKey.getEncoded()));
        String filePath = FileSystemImpl.BASE_PATH + "-" + 0 + File.separatorChar + fileName;
        FileInputStream stream = new FileInputStream(filePath);
        byte[] publicKeyBlock = new byte[BlockUtility.SIGNATURE_SIZE + 1 + BlockUtility.DIGEST_SIZE * 2];
        stream.read(publicKeyBlock, 0, publicKeyBlock.length);
        stream.close();

        // compute data block hashes
        byte[] firstBlock = new byte[BlockUtility.BLOCK_SIZE];
        System.arraycopy(textBytes, 0, firstBlock, 0, BlockUtility.BLOCK_SIZE);
        byte[] secondBlock = new byte[BlockUtility.BLOCK_SIZE];
        System.arraycopy(textBytes, BlockUtility.BLOCK_SIZE, secondBlock, 0, textBytes.length - BlockUtility.BLOCK_SIZE);

        byte[] firstBlockHash = BlockUtility.digest(firstBlock);
        byte[] secondBlockHash = BlockUtility.digest(secondBlock);

        byte[] blockHashes = new byte[BlockUtility.DIGEST_SIZE * 2 + 1];
        blockHashes[0] = (byte) 1;
        System.arraycopy(firstBlockHash, 0, blockHashes, 1, BlockUtility.DIGEST_SIZE);
        System.arraycopy(secondBlockHash, 0, blockHashes, 1 + BlockUtility.DIGEST_SIZE, BlockUtility.DIGEST_SIZE);

        // create signature
        Signature signAlgorithm = Signature.getInstance("SHA512withRSA", "SunRsaSign");
        signAlgorithm.initSign(library.privateKey);
        signAlgorithm.update(blockHashes, 0, blockHashes.length);
        byte[] keyBlockSignature = signAlgorithm.sign();

        byte[] storedSignature = new byte[BlockUtility.SIGNATURE_SIZE];
        System.arraycopy(publicKeyBlock, 0, storedSignature, 0, BlockUtility.SIGNATURE_SIZE);
        assertTrue("The public block's signature is wrong", Arrays.equals(keyBlockSignature, storedSignature));

        byte[] storedFirstHash = new byte[BlockUtility.DIGEST_SIZE];
        System.arraycopy(publicKeyBlock, 1 + BlockUtility.SIGNATURE_SIZE, storedFirstHash, 0, BlockUtility.DIGEST_SIZE);
        assertTrue("The first block's hash is wrong", Arrays.equals(storedFirstHash, firstBlockHash));

        byte[] storedSecondHash = new byte[BlockUtility.DIGEST_SIZE];
        System.arraycopy(publicKeyBlock, 1 + BlockUtility.SIGNATURE_SIZE + BlockUtility.DIGEST_SIZE, storedSecondHash, 0,
                BlockUtility.DIGEST_SIZE);
        assertTrue("The second block's hash is wrong", Arrays.equals(storedSecondHash, secondBlockHash));
    }

    @Test(expected = DataIntegrityFailureException.class)
    public void addDataBlockAttack() throws Exception {
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();

        String text = BlockUtility.generateString(BlockUtility.BLOCK_SIZE);
        byte[] textBytes = text.getBytes();

        library.FS_write(0, textBytes.length, textBytes);

        // get public key block
        String fileName = BlockUtility.getKeyString(BlockUtility.digest(library.publicKey.getEncoded()));
        String filePath = FileSystemImpl.BASE_PATH + "-" + 0 + File.separatorChar + fileName;
        FileInputStream stream = new FileInputStream(filePath);
        byte[] publicKeyBlock = new byte[BlockUtility.SIGNATURE_SIZE + BlockUtility.DIGEST_SIZE];
        stream.read(publicKeyBlock, 0, publicKeyBlock.length);
        stream.close();

        byte[] alteredTextBytes = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        byte[] alteredHash = BlockUtility.digest(alteredTextBytes);

        String newBlockName = BlockUtility.getKeyString(alteredHash);
        String newBlockPath = FileSystemImpl.BASE_PATH + "-" + 0 + File.separatorChar + newBlockName;

        FileOutputStream outStream = new FileOutputStream(newBlockPath);
        outStream.write(alteredTextBytes);
        outStream.close();

        // rewrite public key block to include new data block
        byte[] rewrittenPublicKeyBlock = new byte[BlockUtility.SIGNATURE_SIZE + BlockUtility.DIGEST_SIZE * 2];
        System.arraycopy(publicKeyBlock, 0, rewrittenPublicKeyBlock, 0, BlockUtility.SIGNATURE_SIZE + BlockUtility.DIGEST_SIZE);
        System.arraycopy(alteredHash, 0, rewrittenPublicKeyBlock, BlockUtility.SIGNATURE_SIZE + BlockUtility.DIGEST_SIZE,
                BlockUtility.DIGEST_SIZE);
        outStream = new FileOutputStream(filePath);
        outStream.write(rewrittenPublicKeyBlock);
        outStream.close();

        byte[] readBuffer = new byte[BlockUtility.BLOCK_SIZE];
        library.FS_read(library.publicKey.getEncoded(), 0, BlockUtility.BLOCK_SIZE, readBuffer);

    }

    @Test(expected = DataIntegrityFailureException.class)
    public void changeDataBlockAttack() throws Exception {
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();

        String text = BlockUtility.generateString(BlockUtility.BLOCK_SIZE);
        byte[] textBytes = text.getBytes();

        library.FS_write(0, textBytes.length, textBytes);

        // get data block
        String fileName = BlockUtility.getKeyString(BlockUtility.digest(textBytes));
        String filePath = FileSystemImpl.BASE_PATH + "-" + 0 + File.separatorChar + fileName;
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
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();

        String text = BlockUtility.generateString(BlockUtility.BLOCK_SIZE);
        byte[] textBytes = text.getBytes();
        library.FS_write(0, textBytes.length, textBytes);

        // get public key block
        String fileName = BlockUtility.getKeyString(BlockUtility.digest(library.publicKey.getEncoded()));
        String filePath = FileSystemImpl.BASE_PATH + "-" + 0 + File.separatorChar + fileName;
        FileInputStream stream = new FileInputStream(filePath);
        byte[] publicKeyBlock = new byte[BlockUtility.SIGNATURE_SIZE + 1+BlockUtility.DIGEST_SIZE];
        stream.read(publicKeyBlock, 0, publicKeyBlock.length);
        stream.close();

        byte[] alteredTextBytes = BlockUtility.generateString(BlockUtility.BLOCK_SIZE).getBytes();
        byte[] alteredHash = BlockUtility.digest(alteredTextBytes);

        // write new block
        String newBlockName = BlockUtility.getKeyString(alteredHash);
        String newBlockPath = FileSystemImpl.BASE_PATH + "-" + 0 + File.separatorChar + newBlockName;
        FileOutputStream outStream = new FileOutputStream(newBlockPath);
        outStream.write(alteredTextBytes);
        outStream.close();

        // rewrite public key block to ignore the previous block and point to a new one
        byte[] rewrittenPublicKeyBlock = new byte[BlockUtility.SIGNATURE_SIZE + 1 + BlockUtility.DIGEST_SIZE];
        System.arraycopy(publicKeyBlock, 0, rewrittenPublicKeyBlock, 0, BlockUtility.SIGNATURE_SIZE);
        rewrittenPublicKeyBlock[BlockUtility.SIGNATURE_SIZE] = publicKeyBlock[BlockUtility.SIGNATURE_SIZE];
        System.arraycopy(alteredHash, 0, rewrittenPublicKeyBlock, BlockUtility.SIGNATURE_SIZE, BlockUtility.DIGEST_SIZE);
        outStream = new FileOutputStream(filePath);
        outStream.write(rewrittenPublicKeyBlock);
        outStream.close();

        byte[] readBuffer = new byte[BlockUtility.BLOCK_SIZE];
        library.FS_read(library.publicKey.getEncoded(), 0, BlockUtility.BLOCK_SIZE, readBuffer);
    }

    @Test(expected = DataIntegrityFailureException.class)
    public void deleteDataBlockAttack() throws Exception {
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();

        String text = BlockUtility.generateString(BlockUtility.BLOCK_SIZE);
        byte[] textBytes = text.getBytes();
        library.FS_write(0, textBytes.length, textBytes);

        // delete data block
        String fileName = BlockUtility.getKeyString(BlockUtility.digest(textBytes));
        String filePath = FileSystemImpl.BASE_PATH + "-" + 0 + File.separatorChar + fileName;
        System.out.println("Deleting file " + fileName);
        File dataBlock = new File(filePath);

        assertTrue("The test couldn't delete the file - invalid", dataBlock.delete());

        byte[] readBuffer = new byte[BlockUtility.BLOCK_SIZE];
        library.FS_read(library.publicKey.getEncoded(), 0, BlockUtility.BLOCK_SIZE, readBuffer);
    }
}
