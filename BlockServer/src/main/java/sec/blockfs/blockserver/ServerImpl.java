package sec.blockfs.blockserver;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerImpl extends UnicastRemoteObject implements BlockServer{
	public ServerImpl() throws RemoteException {
		super();
	}

	public static void main (String args[]) {
        try {
        	String servicePort = args[0];
        	String serviceName = args[1];
        	
	        Registry registry = LocateRegistry.createRegistry(new Integer(servicePort));
            registry.rebind(serviceName, new ServerImpl());
            System.out.println("Server initiated");
            System.in.read();
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
	}

	public byte[] get(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	public int put_k(byte[] data, String signature, String pub_key) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int put_h(byte[] data) {
		// TODO Auto-generated method stub
		return 0;
	}

}
