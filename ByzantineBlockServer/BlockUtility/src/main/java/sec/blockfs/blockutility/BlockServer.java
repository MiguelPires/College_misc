package sec.blockfs.blockutility;

import java.io.FileNotFoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BlockServer extends Remote {
    // returns a stored data block
    byte[] get(String id) throws RemoteException, WrongArgumentsException, ServerErrorException, FileNotFoundException;

    // stores a signed, public key block
    String put_k(byte[] data, byte[] signature, byte[] pub, String libraryUrl, String libraryName, int libraryPort)
            throws RemoteException, ServerErrorException, DataIntegrityFailureException;

    // stores a content hash block
    String put_h(byte[] data) throws RemoteException, ServerErrorException;
}
