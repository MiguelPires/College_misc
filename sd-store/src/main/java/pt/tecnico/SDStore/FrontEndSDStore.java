package pt.tecnico.SDStore;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import javax.jws.*;
import javax.xml.ws.BindingProvider;
import pt.tecnico.ws.uddi.UDDINaming;

import pt.ulisboa.tecnico.sdis.store.ws.*; // classes generated from WSDL
import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.net.SocketTimeoutException;


public class FrontEndSDStore implements SDStore {

	private String uddiURL;
	private String name;
	private String endpointAddress;

	private UDDINaming uddiNaming;
	private SDStore_Service service; 
    private SDStore server;
    private SDStore lastServer;

	private List<SDStore> repManager = new ArrayList<SDStore>();

	
	public FrontEndSDStore(String uddiURL, String name) throws Exception { 
		
		this.uddiURL = uddiURL;
		this.name = name;

		System.out.printf("Contacting UDDI at %s%n", uddiURL);
        uddiNaming = new UDDINaming(uddiURL);

        for(int num = 0; num < 3 ; num++) {
            	
        	System.out.printf("Looking for '%s'%n", name + "" + num);
        	endpointAddress = uddiNaming.lookup(name + "" + num);  

        	if (endpointAddress == null) {
        		System.out.printf("Server not Found!");
        	}
        	else {
				
				System.out.printf("Found URL: %s%n", endpointAddress);
            	
            	System.out.println("Creating stub ...");
				service = new SDStore_Service();
                server = service.getSDStoreImplPort();
				
				System.out.println("Setting endpoint address ...");
				BindingProvider bp = (BindingProvider) server;
				Map<String, Object> requestContext = bp.getRequestContext(); 
				requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
				
				repManager.add(server);
			}	
        }
        System.out.println("Remote call ...");
    }


    // Send client listDocs request to all replica managers and returns the last one response
 	public List<String> listDocs(String name) throws UserDoesNotExist_Exception {
    
    	int aux = 0;
		for(SDStore server : repManager) {
			server.listDocs(name);
			aux++;
		}

		lastServer = repManager.get(aux);
		return lastServer.listDocs(name);
    }

    // Send client createDoc request to all replica managers 
    public void createDoc(DocUserPair docUserPair) throws DocAlreadyExists_Exception {
		
		for(SDStore server : repManager) {
			server.createDoc(docUserPair);
		}
	}

	// Send client store request to all replica managers
	public void store(DocUserPair docUserPair, byte[] contents) throws CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception {
	
		for(SDStore server : repManager) {
			server.store(docUserPair, contents);
		}		
	}

	// Send client load request to all replica managers and returns the last one response
	public byte[] load(DocUserPair docUserPair) throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
		
		int aux = 0;
		for(SDStore server : repManager) {
			server.load(docUserPair);
			aux++;			
		}
		
		lastServer = repManager.get(aux);
		return lastServer.load(docUserPair);
	}


}

