package sec.blockfs.blockserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BlockServer extends Remote {
	// returns a stored data block
	byte[] get(int id) throws RemoteException;
	//stores a signed, public key block
	int put_k(byte[] data, String signature, String pub_key) throws RemoteException;
	// stores a content hash block
	int put_h(byte[] data) throws RemoteException;
}
