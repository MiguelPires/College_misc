package pt.tecnico.bubbledocs.service.remote;

import javax.xml.registry.JAXRException;

import pt.tecnico.bubbledocs.domain.Content;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.CannotLoadDocumentException;
import pt.tecnico.bubbledocs.exception.CannotStoreDocumentException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.SDStore.StoreClient;
import pt.ulisboa.tecnico.sdis.store.ws.*;



public class StoreRemoteServices {
	
	private StoreClient storeCliente;
	private DocUserPair docUserPair;
	private User user;
	private String uddiURL = "http://localhost:8081";
	private int id = 1;

    public void storeDocument(String username, String docName, byte[] document)
                                                                            throws CannotStoreDocumentException,
                                                                            RemoteInvocationException {
    	storeCliente = new SDStoreClient(uddiURL, username, id);
    	storeCliente.createDoc(docUserPair);
    	docUserPair.setUserId(username);
    	docUserPair.setDocId(docName);
    	try{
    		storeCliente.store(docUserPair, document);
    	}catch (JAXRException e) {
        throw new RemoteInvocationException(e.getMessage());
    }
    }

    public byte[] loadDocument(String username, String docName) throws CannotLoadDocumentException,
                                                               RemoteInvocationException {
    	docUserPair.getUserId(username);
    	try{
    		return storeCliente.load(docUserPair.getDocId(docName));
    	}catch (JAXRException e) {
            throw new RemoteInvocationException(e.getMessage());
        }
    }
}
