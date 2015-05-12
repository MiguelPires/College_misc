package pt.tecnico.SDStore;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import javax.jws.*;
import javax.xml.registry.JAXRException;

import java.util.concurrent.ExecutionException;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Response;

import com.sun.xml.ws.api.EndpointAddress;

import pt.tecnico.SDStore.handler.ClientHandler;
import pt.tecnico.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.store.ws.*; // classes generated from WSDL
import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

public class FrontEndSDStore {

	public List<SDStore> repManager = new ArrayList<SDStore>();
	private int WT=0;
	private int RT=0;
	private int IDClient;
	private int sequencial = 0;
	
	public void setWT(){
		while(2*WT<= repManager.size())
			WT++;
	}
	
	public void setRT(){
		while((WT+RT)<= repManager.size())
			RT++;
	}
	
	public FrontEndSDStore(String uddiURL, String name, int ClientID) throws JAXRException { 
		
		IDClient = ClientID;
		System.out.printf("Contacting UDDI at %s%n", uddiURL);
		UDDINaming uddiNaming = null;
       
		uddiNaming = new UDDINaming(uddiURL);
	

        String endpointAddress = null;
        for(int num = 0; num < 3 ; num++) {
            	
        	System.out.printf("Looking for '%s'%n", name + num);
       
			endpointAddress = uddiNaming.lookup(name + num);
			

        	if (endpointAddress == null) {
        		System.out.println("Server " + name + num + " not Found!");
        	}
        	else {
				
				System.out.printf("Found URL: %s%n", endpointAddress);
            	
            	System.out.println("Creating stub ...");
            	SDStore_Service service = new SDStore_Service();
                SDStore server = service.getSDStoreImplPort();
				
				System.out.println("Setting endpoint address ...");
				BindingProvider bp = (BindingProvider) server;
				Map<String, Object> requestContext = bp.getRequestContext(); 
				requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
				
				repManager.add(server);
			}	
        }
        
        setWT();
        setRT();
        System.out.println("Ready");
    }


    // Send client listDocs request to all replica managers and returns the last one response
 	public List<String> listDocs(String name) throws UserDoesNotExist_Exception {
    
 		List<String> docs = new ArrayList<String>();
		for(SDStore server : repManager) 
			docs = server.listDocs(name);

		return docs;
    }

    // Send client createDoc request to all replica managers 
    public void createDoc(DocUserPair docUserPair) throws DocAlreadyExists_Exception {
		
		for(SDStore server : repManager) {
			server.createDoc(docUserPair);
		}
	}

    public void store(DocUserPair docUserPair, byte[] contents, String[] tag) throws CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception {
    	if(tag==null){
    		sequencial++;
    		String[] newTag={sequencial+"", IDClient+""};
    		tag = newTag;
    		
    		List<Response<LoadResponse>> LoadResponses = new ArrayList<Response<LoadResponse>>();
    		for(SDStore server : repManager) {
    			sendToHandler(server, tag);
    			Response<LoadResponse> response = server.loadAsync(docUserPair);
    			LoadResponses.add(response);
    		}
    		
    		int numberResponses=0;
    		while(numberResponses<RT) // reads max tag
    			for(Response<LoadResponse> response : LoadResponses){
    				if(response.isDone()){
    					numberResponses++;
    					String[] maxTag=null;
    					if(hasTag(response))
    						maxTag = getTag(response);
    					if(maxTag != null && isGreater(maxTag,tag)){
    						tag = maxTag;
    					}
    					LoadResponses.remove(response);
    					break;
    				}
    			}
    		
    		
    	}
    	else
    		sequencial = Integer.parseInt(tag[0]);
    		
    	List<Response<StoreResponse>> StoreResponses = new ArrayList<Response<StoreResponse>>();
    	for(SDStore server : repManager) {
    		sendToHandler(server, tag);
			Response<StoreResponse> response  = server.storeAsync(docUserPair, contents);
			StoreResponses.add(response);
		}	
    	
    	//waits for Q acks
    	int numberResponses=0;
    	Response<StoreResponse> savedResponse=null;
		while(numberResponses<WT)
			for(Response<StoreResponse> response : StoreResponses){
				if(response.isDone()){
					numberResponses++;
					StoreResponses.remove(response);
					savedResponse=response;
					break;
				}	
			}
    	
		if(savedResponse!=null)
			try {
				savedResponse.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				String[] exc = e.getMessage().split(":");
				throwException(exc, docUserPair);
			}
    }
    
	// Send client store request to all replica managers
	public void store(DocUserPair docUserPair, byte[] contents) throws CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception {
	
		store(docUserPair, contents, null);	
	}

	// Send client load request to all replica managers and returns the last one response
	public byte[] load(DocUserPair docUserPair) throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
		
		List<Response<LoadResponse>> LoadResponses = new ArrayList<Response<LoadResponse>>();
		sequencial++;
		String[] tag = {sequencial+"",IDClient+""};
		for(SDStore server : repManager) {
			sendToHandler(server, tag);
			Response<LoadResponse> response = server.loadAsync(docUserPair);
			LoadResponses.add(response);
		}
		
		//wait for Q responses and stores highest tag and value
		int numberResponses = 0;
		tag=null;
		byte[] value=null;
		while(numberResponses<RT)
			for(Response<LoadResponse> response : LoadResponses){
				if(response.isDone()){
					numberResponses++;
					String[] newTag=null;
					if(hasTag(response))
						newTag = getTag(response);
					if(tag == null || isGreater(newTag,tag)){
						tag = newTag;
						try {
							value = response.get().getContents();
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) { // catch and checks wich exceptions it is
							String[] exc = e.getMessage().split(":");
							throwException(exc, docUserPair);
						}
					}
					LoadResponses.remove(response);
					break;
				}
			}
		
		try {
			store(docUserPair, value, tag);
		} catch (CapacityExceeded_Exception e) {
			System.out.println("capacity is deactivated; should never go here");
			e.printStackTrace();
		}
		
		return value;
		}

	public void sendToHandler(SDStore port, String[] tag){
		BindingProvider bindingProvider = (BindingProvider) port;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        String initialValue = tag[0]+";"+tag[1];
        requestContext.put(ClientHandler.REQUEST_PROPERTY, initialValue);
	}
	
	public String[] getTag(Response<LoadResponse> response){
		Map<String, Object> responseContext = response.getContext();
        String propertyValue = (String) responseContext.get(ClientHandler.RESPONSE_PROPERTY);
        String[] result = {propertyValue.split(";")[0].split(":")[1], propertyValue.split(";")[1].split(":")[1]};
        return result;
	}
	
	public boolean hasTag(Response<LoadResponse> response){
		Map<String, Object> responseContext = response.getContext();
        String propertyValue = (String) responseContext.get(ClientHandler.RESPONSE_PROPERTY);
        if(propertyValue!=null)
        	return true;
        else
        	return false;
	}
	
	//compare 2 tags
	public boolean isGreater(String[] tag1, String[] tag2){
		int seqNumber1 = Integer.parseInt(tag1[0]);
		int clientID1 = Integer.parseInt(tag1[1]);
		int seqNumber2 = Integer.parseInt(tag2[0]);
		int clientID2 = Integer.parseInt(tag2[1]);
		if(seqNumber1 > seqNumber2)
			return true;
		else if(seqNumber1 == seqNumber2)
			if(clientID1 > clientID2)
				return true;
		
		return false;
	}
	
	public void throwException(String[] exc, DocUserPair docUserPair) throws UserDoesNotExist_Exception, DocDoesNotExist_Exception{
		if(exc[1].equals(" User does not exist")){
			UserDoesNotExist userException = new UserDoesNotExist();
			userException.setUserId(docUserPair.getUserId());
			throw new UserDoesNotExist_Exception("User does not exist", userException);	
		}
		else{
			DocDoesNotExist doc = new DocDoesNotExist();
			doc.setDocId(docUserPair.getDocumentId());
			throw new DocDoesNotExist_Exception("Document does not exist", doc);
		}
	}
}

