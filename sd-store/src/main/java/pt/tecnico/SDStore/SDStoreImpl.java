package pt.tecnico.SDStore;

import java.util.List;

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

    
    public List<String> listDocs(String name) {
    	 System.out.println("creating " + name + "!");
    	 return null;
    }
    

	public void createDoc(DocUserPair docUserPair)
			throws DocAlreadyExists_Exception {
		 System.out.println("creating !");
		
	}

	public void store(DocUserPair docUserPair, byte[] contents)
			throws CapacityExceeded_Exception, DocDoesNotExist_Exception,
			UserDoesNotExist_Exception {
		 System.out.println("creating!");
		
	}

	public byte[] load(DocUserPair docUserPair)
			throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
		 System.out.println("creating !");
    	 return null;
	}

}
