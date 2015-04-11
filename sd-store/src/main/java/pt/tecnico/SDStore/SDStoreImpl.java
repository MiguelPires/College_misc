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

private ArrayList <userDirectory> folders;


	public SDStoreImpl(){
		folders = new ArrayList<userDirectory>();
		setup();
	}
	
	public void setup(){
		folders.add(new userDirectory("alice"));
		folders.add(new userDirectory("bruno"));
		folders.add(new userDirectory("carla"));
		folders.add(new userDirectory("duarte"));
		folders.add(new userDirectory("eduardo"));
	}

    public List<String> listDocs(String name) {
    	//TO-DO
    	 return null;
    }
    

    //creates document for user; if user does not exists, creates user; if document already exists, throws exception
	public void createDoc(DocUserPair docUserPair) throws DocAlreadyExists_Exception {
		String user = docUserPair.getUserId();
		String doc = docUserPair.getDocumentId();
		userDirectory folder = null;
				
		for(userDirectory aux : folders)
			if(aux.getUser().equals(user))
				folder = aux;
			
		if(folder==null){
			folder = new userDirectory(user);
			folders.add(folder);
		}
		
		folder.addDoc(doc);		
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
