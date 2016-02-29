package sec.blockfs.blockserver;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.junit.Test;

public class RMIServerTest {
    private static String servicePort = System.getProperty("service.port");
    private static String serviceName = System.getProperty("service.name");
    private static String serviceUrl = System.getProperty("service.url");

    @Test
    public void success() throws Exception {
        Registry registry = LocateRegistry.createRegistry(new Integer(servicePort));
        registry.rebind(serviceName, new ServerImpl());
    }
}
