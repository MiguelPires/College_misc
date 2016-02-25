package sec.blockfs.blocklibrary;

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

public class RMIClientTest {
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
	public void tearDown() throws AccessException, RemoteException, NotBoundException {
		if (registry != null)
			registry.unbind(serviceName);
	}

	@Test
	public void sucess() throws Exception {
		Naming.lookup(serviceUrl + ":" + servicePort + "/" + serviceName);
	}

	@Test(expected = java.rmi.ConnectException.class)
	public void wrongPort() throws Exception {
		Naming.lookup(serviceUrl + ":" + (servicePort + 1) + "/" + serviceName);
	}

	@Test(expected = java.rmi.ConnectException.class)
	public void wrongName() throws Exception {
		Naming.lookup(serviceUrl + ":" + (servicePort + 1) + "/" + serviceName + "abc");
	}
}
