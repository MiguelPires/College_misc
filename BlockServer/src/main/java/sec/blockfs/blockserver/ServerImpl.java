package sec.blockfs.blockserver;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerImpl extends UnicastRemoteObject implements BlockServer{

	protected ServerImpl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	public static void main (String args[]) {
        try {
	        Registry registry = LocateRegistry.createRegistry(5789);

            registry.rebind("BlockFileSystem", new ServerImpl());
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
