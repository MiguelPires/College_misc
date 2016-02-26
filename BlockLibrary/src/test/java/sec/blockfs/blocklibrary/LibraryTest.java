package sec.blockfs.blocklibrary;

import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sec.blockfs.blockserver.ServerImpl;

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
}
