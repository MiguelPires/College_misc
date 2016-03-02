package sec.blockfs.blockserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BlockServer extends Remote {
    // returns a stored data block
    byte[] get(byte[] publicKeyBytes) throws RemoteException, WrongArgumentsException, ServerErrorException, DataIntegrityFailureException;

    // stores a signed, public key block
    byte[] put_k(byte[] data, byte[] signature, byte[] pub)
            throws RemoteException, ServerErrorException, DataIntegrityFailureException;

    // stores a content hash block
    byte[] put_h(byte[] data) throws RemoteException, ServerErrorException;
}
