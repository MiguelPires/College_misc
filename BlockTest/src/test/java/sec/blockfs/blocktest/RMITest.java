package sec.blockfs.blocktest;

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

import sec.blockfs.blocklibrary.BlockLibraryImpl;
import sec.blockfs.blocklibrary.InitializationFailureException;
import sec.blockfs.blockserver.ServerImpl;

public class RMITest {
    private static String servicePort = System.getProperty("service.port");
    private static String serviceName = System.getProperty("service.name");
    private static String serviceUrl = System.getProperty("service.url");
    private BlockLibraryImpl library;
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
            } catch (Exception e) {
            }
            try {

                registry.unbind(serviceName);
            } catch (Exception e) {
            }

            /*  try {
                library.pkcs11.C_CloseSession(library.sessionToken);
            } catch (PKCS11Exception e) {
                e.printStackTrace();
            }*/
        }
    }

    @Test(expected = InitializationFailureException.class)
    public void wrongPort() throws Exception {
        library = new BlockLibraryImpl(serviceName, servicePort + 1, serviceUrl);
        library.FS_init();
    }

    @Test(expected = InitializationFailureException.class)
    public void wrongName() throws Exception {
        library = new BlockLibraryImpl(serviceName + "a", servicePort, serviceUrl);
        library.FS_init();
    }
}
