package sec.blockfs.blockserver;

import java.io.FileNotFoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import sec.blockfs.blockutility.OperationFailedException;

public interface BlockServer extends Remote {
    // returns a stored data block
    byte[] get(String id) throws RemoteException, WrongArgumentsException, ServerErrorException, FileNotFoundException;

    // stores a signed, public key block
    String put_k(byte[] data, byte[] signature, byte[] pub)
            throws RemoteException, ServerErrorException, DataIntegrityFailureException;

    // stores a content hash block
    String put_h(byte[] data) throws RemoteException, ServerErrorException;

    // stores a public key
    void storePubKey(X509Certificate certificate, ArrayList<X509Certificate> supplierCertificates) throws RemoteException, DataIntegrityFailureException, ServerErrorException;
    
    // list every public key in the server
    List<X509Certificate> readPubkeys() throws RemoteException;
}
