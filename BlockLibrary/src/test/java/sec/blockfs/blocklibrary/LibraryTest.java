package sec.blockfs.blocklibrary;

import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.util.Base64;

import javax.swing.text.html.BlockView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sec.blockfs.blockserver.FileSystemImpl;
import sec.blockfs.blockserver.ServerImpl;
import sec.blockfs.blockutility.BlockUtility;

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
    public void tearDown()
            throws AccessException, RemoteException, NotBoundException, MalformedURLException {
        if (registry != null) {
            try {
                Naming.unbind(serviceName);
                registry.unbind(serviceName);
            } catch (NotBoundException e) {
                return;
            }
        }
    }

    @Test
    public void successCreateLib() throws Exception {
        new BlockLibrary(serviceName, servicePort, serviceUrl);
    }

    @Test(expected = java.rmi.ConnectException.class)
    public void wrongPort() throws Exception {
        new BlockLibrary(serviceName, servicePort + 1, serviceUrl);
    }

    @Test(expected = java.rmi.NotBoundException.class)
    public void wrongName() throws Exception {
        new BlockLibrary(serviceName + "abc", servicePort, serviceUrl);
    }

    @Test
    public void successPerformWrite() throws Exception {
        String text = "Some random write";
        byte[] textBytes = text.getBytes();
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.write(textBytes);
    }

    @Test
    public void successEmptyWrite() throws Exception {
        byte[] textBytes = "".getBytes();
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.write(textBytes);
    }

    @Test(expected = OperationFailedException.class)
    public void failNullWrite() throws Exception {
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.write(null);
    }

    @Test
    public void publicKeyBlockCheck() throws Exception {
        // write a message 
        String text = "Some random content";
        byte[] textBytes = text.getBytes();
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.write(textBytes);
        
        // compute hash of public key
        byte[] keyDigest = BlockUtility.clearAndCompute(library.publicKey.getEncoded());
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
        BlockLibrary library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.write(textBytes);
        
        // compute hash of data
        byte[] keyDigest = BlockUtility.clearAndCompute(textBytes);
        String fileName = BlockUtility.getKeyString(keyDigest);
        
        // check for data block
        String filePath = FileSystemImpl.BASE_PATH + File.separatorChar + fileName;
        File file = new File(filePath);
        assertTrue("Data block '" + filePath + "' doesn't exist", file.exists());
    }
    
    @Test
    public void publicKeyBlockContentsCheck() {
        //TODO
    }
    
    @Test
    public void dataBlockContentsCheck() {
        //TODO
    }
}
