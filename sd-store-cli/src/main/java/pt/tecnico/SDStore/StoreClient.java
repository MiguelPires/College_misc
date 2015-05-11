package pt.tecnico.SDStore;

import java.util.*;

import javax.xml.registry.JAXRException;

import pt.ulisboa.tecnico.sdis.store.ws.*;

public class StoreClient {

    private FrontEndSDStore front;

    public StoreClient(String uddiURL, String name, int id) throws JAXRException{
        front = new FrontEndSDStore(uddiURL, name, id);
    }

    public StoreClient(String uddiURL, String name) throws JAXRException{
        front = new FrontEndSDStore(uddiURL, name, 1);
    }

    public List<String> listDocs(String name) throws UserDoesNotExist_Exception {
    	return front.listDocs(name);
    }
    
    public void createDoc(DocUserPair docUserPair) throws DocAlreadyExists_Exception {
    	front.createDoc(docUserPair);
    }
    
	public void store(DocUserPair docUserPair, byte[] contents) throws CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception {
		front.store(docUserPair, contents);
	}
	
	public byte[] load(DocUserPair docUserPair) throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
		return front.load(docUserPair);
	}
}
