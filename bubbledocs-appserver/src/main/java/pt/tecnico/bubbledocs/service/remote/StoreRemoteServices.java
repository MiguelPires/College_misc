package pt.tecnico.bubbledocs.service.remote;
import pt.tecnico.bubbledocs.exception.CannotStoreDocumentException;
import pt.tecnico.bubbledocs.exception.CannotLoadDocumentException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;


public class StoreRemoteServices {

    public void storeDocument(String username, String docName, byte[] document)
        throws CannotStoreDocumentException, RemoteInvocationException {
        // TODO : the connection and invocation of the remote service
    }

    public byte[] loadDocument(String username, String docName)
        throws CannotLoadDocumentException, RemoteInvocationException {
        // TODO : the connection and invocation of the remote service
    	return null;
    }
}
