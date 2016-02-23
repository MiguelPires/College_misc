package sec.blockfs.blockserver;

import java.rmi.Remote;

public interface BlockServer extends Remote {
	// returns a stored data block
	byte[] get(int id);
	//stores a signed, public key block
	int put_k(byte[] data, String signature, String pub_key);
	// stores a content hash block
	int put_h(byte[] data);
}
