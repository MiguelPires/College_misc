package sec.blockfs.blocklibrary;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sec.blockfs.blockserver.FileSystemImpl;
import sec.blockfs.blockserver.ServerImpl;
import sec.blockfs.blockutility.BlockUtility;
import sec.blockfs.blockutility.OperationFailedException;

public class LibraryTest {
    private static String servicePort = System.getProperty("service.port");
    private static String serviceName = System.getProperty("service.name");
    private static String serviceUrl = System.getProperty("service.url");
    private Registry registry;

    @Before
    public void setUp() throws NumberFormatException, RemoteException {
        try {
            registry = LocateRegistry.createRegistry(new Integer(servicePort));
            registry.rebind(serviceName, new ServerImpl());
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
        BlockLibrary library = new BlockLibrary();
        library.FS_init(serviceName, servicePort, serviceUrl);
    }

    @Test(expected = java.rmi.ConnectException.class)
    public void wrongPort() throws Exception {
        BlockLibrary library = new BlockLibrary();
        library.FS_init(serviceName, servicePort + 1, serviceUrl);
    }

    @Test(expected = java.rmi.NotBoundException.class)
    public void wrongName() throws Exception {
        BlockLibrary library = new BlockLibrary();
        library.FS_init(serviceName + "abc", servicePort, serviceUrl);
    }

    @Test
    public void successPerformWrite() throws Exception {
        String text = "Some random write";
        byte[] textBytes = text.getBytes();
        BlockLibrary library = new BlockLibrary();
        library.FS_init(serviceName, servicePort, serviceUrl);
        library.FS_write(0, textBytes.length, textBytes);
    }

    @Test
    public void successEmptyWrite() throws Exception {
        byte[] textBytes = "".getBytes();
        BlockLibrary library = new BlockLibrary();
        library.FS_init(serviceName, servicePort, serviceUrl);
        library.FS_write(0, textBytes.length, textBytes);
    }

    @Test(expected = OperationFailedException.class)
    public void failNullWrite() throws Exception {
        BlockLibrary library = new BlockLibrary();
        library.FS_init(serviceName, servicePort, serviceUrl);
        library.FS_write(0, 0, null);
    }

    @Test(expected = OperationFailedException.class)
    public void failNegativeSizeArgument() throws Exception {
        BlockLibrary library = new BlockLibrary();
        library.FS_init(serviceName, servicePort, serviceUrl);
        library.FS_write(0, -1, "".getBytes());
    }

    @Test(expected = OperationFailedException.class)
    public void failNegativeOffsetArgument() throws Exception {
        BlockLibrary library = new BlockLibrary();
        library.FS_init(serviceName, servicePort, serviceUrl);
        library.FS_write(-1, 0, "".getBytes());
    }

    @Test
    public void publicKeyBlockCheck() throws Exception {
        // write a message
        String text = "Some random content";
        byte[] textBytes = text.getBytes();
        BlockLibrary library = new BlockLibrary();
        library.FS_init(serviceName, servicePort, serviceUrl);
        library.FS_write(0, textBytes.length, textBytes);

        // compute hash of public key
        byte[] keyDigest = BlockUtility.digest(library.publicKey.getEncoded());
        String fileName = BlockUtility.getKeyString(keyDigest);

        // check for public key block
        String filePath = FileSystemImpl.BASE_PATH + File.separatorChar + fileName;
        File file = new File(filePath);
        assertTrue("Public key block '" + filePath + "' doesn't exist", file.exists());
    }

    @Test
    public void dataBlockCheck() throws Exception {
        // write a message
        String text = "Some random content";
        byte[] textBytes = text.getBytes();
        BlockLibrary library = new BlockLibrary();
        library.FS_init(serviceName, servicePort, serviceUrl);
        library.FS_write(0, textBytes.length, textBytes);

        // compute hash of data
        byte[] dataDigest = BlockUtility.digest(textBytes);
        String fileName = BlockUtility.getKeyString(dataDigest);

        // check for data block
        String filePath = FileSystemImpl.BASE_PATH + File.separatorChar + fileName;
        File file = new File(filePath);
        assertTrue("Data block '" + filePath + "' doesn't exist", file.exists());
    }

    @Test
    public void publicKeyBlockContentsCheck() throws Exception {
        // write a message
        String text = "Some random content";
        byte[] textBytes = text.getBytes();
        BlockLibrary library = new BlockLibrary();
        library.FS_init(serviceName, servicePort, serviceUrl);
        library.FS_write(0, textBytes.length, textBytes);

        byte[] buffer = new byte[textBytes.length];
        int bytesRead = library.FS_read(library.publicKey.getEncoded(), 0, textBytes.length, buffer);
        assertTrue("Read returned wrong data: "+Arrays.toString(buffer)+"; Expected: "+Arrays.toString(textBytes), Arrays.equals(buffer, textBytes));

        // compute hash of public key
        byte[] keyDigest = BlockUtility.digest(library.publicKey.getEncoded());
        String fileName = BlockUtility.getKeyString(keyDigest);
        String filePath = FileSystemImpl.BASE_PATH + File.separatorChar + fileName;
        FileInputStream stream = new FileInputStream(filePath);
        byte[] publicBlock = new byte[BlockUtility.SIGNATURE_SIZE+BlockUtility.DIGEST_SIZE];
        stream.read(publicBlock);
        stream.close();
        
        // extract data
        byte[] publicKeyData = new byte[BlockUtility.DIGEST_SIZE];
        System.arraycopy(publicBlock, BlockUtility.SIGNATURE_SIZE, publicKeyData, 0, publicKeyData.length);
        
        assertTrue("Public key block contains wrong data", Arrays.equals(publicKeyData, BlockUtility.digest(textBytes)));
    }

    @Test
    public void dataBlockContentsCheck() throws Exception {
        // write a message
        String text = "Some random content";
        byte[] textBytes = text.getBytes();
        BlockLibrary library = new BlockLibrary();
        library.FS_init(serviceName, servicePort, serviceUrl);
        library.FS_write(0, textBytes.length, textBytes);

        // hash of data - expected contents
        byte[] dataDigest = BlockUtility.digest(textBytes);
        String fileName = BlockUtility.getKeyString(dataDigest);
        String filePath = FileSystemImpl.BASE_PATH + File.separatorChar + fileName;

        // verify data block contents
        FileInputStream stream = new FileInputStream(filePath);
        byte[] buffer = new byte[textBytes.length];
        stream.read(buffer, 0, textBytes.length);
        stream.close();
        assertTrue("Data block contains wrong data", Arrays.equals(buffer, textBytes));
    }
}
