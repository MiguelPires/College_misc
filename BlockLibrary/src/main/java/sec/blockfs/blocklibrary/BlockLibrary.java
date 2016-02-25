package sec.blockfs.blocklibrary;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import sec.blockfs.blockserver.BlockServer;

public class BlockLibrary {
	private static BlockServer blockServer;

	public static void main(String[] args) {
		try {
			String servicePort = args[0];
			String serviceName = args[1];
			String serviceUrl = args[2];
			blockServer = (BlockServer) Naming.lookup(serviceUrl+":"+servicePort+"/"+serviceName);
			System.out.println("Connected to block server");
			System.in.read();
		} catch (Exception e) {
			System.err.println("Connection failed: ");
			e.printStackTrace();
		}
	}	
}
