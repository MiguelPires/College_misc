package pt.tecnico.SDStore;

import java.util.List;
import java.util.ArrayList;

import javax.jws.*;

import pt.ulisboa.tecnico.sdis.store.ws.*; // classes generated from WSDL

@WebService(
    endpointInterface="pt.ulisboa.tecnico.sdis.store.ws.SDStore", 
    wsdlLocation="SD-STORE.1_1.wsdl",
    name="SDStore",
    portName="SDStoreImplPort",
    targetNamespace="urn:pt:ulisboa:tecnico:sdis:store:ws",
    serviceName="SDStore"
)

public class SDStoreImpl implements SDStore {

private ArrayList <String> users;
private ArrayList <userDirectory> folders;


	public SDStoreImpl(){
		users = new ArrayList<String>();
		folders = new ArrayList<userDirectory>();
	}

    public List<String> listDocs(String name) {
    	//TO-DO
    	 return null;
    }
    

    //creates document for user; if user does not exists, creates user; if document already exists, throws exception
	public void createDoc(DocUserPair docUserPair)
			throws DocAlreadyExists_Exception {
		String user = docUserPair.getUserId();
		String doc = docUserPair.getDocumentId();
		userDirectory folder = null;
		
		if(!users.contains(user)){
			users.add(user);
			folder = new userDirectory(user);
			folders.add(folder);
		}
		else{
			for(userDirectory aux : folders)
				if(aux.getUser().equals(user))
					folder = aux;
		}
		
		if(folder.docExists(doc)){
			DocAlreadyExists faultinfo = new DocAlreadyExists();
			faultinfo.setDocId(doc);
			throw new DocAlreadyExists_Exception("Document ID already exists", faultinfo);
		}
		else{
			folder.addDoc(doc);
		}	
	}

	public void store(DocUserPair docUserPair, byte[] contents)
			throws CapacityExceeded_Exception, DocDoesNotExist_Exception,
			UserDoesNotExist_Exception {
		//TO-DO
		
	}

	public byte[] load(DocUserPair docUserPair)
			throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
				//TO-DO
    	 return null;
	}


}
