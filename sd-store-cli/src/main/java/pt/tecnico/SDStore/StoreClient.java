package pt.tecnico.SDStore;

import java.util.*;

import javax.xml.registry.JAXRException;

import pt.ulisboa.tecnico.sdis.store.ws.*;

public class StoreClient {

    public FrontEndSDStore front;

    public StoreClient(String uddiURL, String name, int id) throws JAXRException{
        front = new FrontEndSDStore(uddiURL, name, id);
    }

    public StoreClient(String uddiURL, String name) throws JAXRException{
        front = new FrontEndSDStore(uddiURL, name, 1);
    }

    public List<String> listDocs(String name) throws UserDoesNotExist_Exception {
    	return front.listDocs(name);
    }
    
    public void createDoc(String username, String docId) throws DocAlreadyExists_Exception {
        DocUserPair pair = new DocUserPair();
        pair.setUserId(username);
        pair.setDocumentId(docId);
    	front.createDoc(pair);
    }
    
	public void store(String username, String docId, byte[] contents) throws CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception {
		DocUserPair pair = new DocUserPair();
        pair.setUserId(username);
        pair.setDocumentId(docId);
        try{
        	front.store(pair, contents);
        } catch (DocDoesNotExist_Exception e){
        	try {
				createDoc(username, docId);
			} catch (DocAlreadyExists_Exception e1) {
				e1.printStackTrace();
			}
        	front.store(pair, contents);
        }
	}
	
	public byte[] load(String username, String docId) throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
        DocUserPair pair = new DocUserPair();
        pair.setUserId(username);
        pair.setDocumentId(docId);
		return front.load(pair);
	}
}
