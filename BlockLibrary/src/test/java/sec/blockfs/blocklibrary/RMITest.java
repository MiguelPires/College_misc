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

import pteidlib.pteid;
import sec.blockfs.blockserver.ServerImpl;
import sun.security.pkcs11.wrapper.PKCS11Exception;

public class RMITest {
    private static String servicePort = System.getProperty("service.port");
    private static String serviceName = System.getProperty("service.name");
    private static String serviceUrl = System.getProperty("service.url");
    private BlockLibrary library;
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

        try {
            library.pkcs11.C_Logout(library.sessionToken);
            pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); 
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

   /* @Test
    public void successCreateLib() throws Exception {
        library = new BlockLibrary(serviceName, servicePort, serviceUrl);
        library.FS_init();
    }*/

    @Test(expected = InitializationFailureException.class)
    public void wrongPort() throws Exception {
        library = new BlockLibrary(serviceName, servicePort + 1, serviceUrl);
        library.FS_init();
    }

    @Test(expected = InitializationFailureException.class)
    public void wrongName() throws Exception {
        library = new BlockLibrary(serviceName+"a", servicePort, serviceUrl);
        library.FS_init();
    }
}
