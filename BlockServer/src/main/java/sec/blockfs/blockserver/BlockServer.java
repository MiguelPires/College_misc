package sec.blockfs.blockserver;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

public interface BlockServer extends Remote {
  // returns a stored data block
  byte[] get(int id) throws RemoteException;

  // stores a signed, public key block
  byte[] put_k(byte[] data, byte[] signature, byte[] pub) throws RemoteException, ServerErrorException;

  // stores a content hash block
  int put_h(byte[] data) throws RemoteException;
}
