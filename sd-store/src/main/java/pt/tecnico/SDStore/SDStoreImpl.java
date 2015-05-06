package pt.tecnico.SDStore;

import java.util.List;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.jws.*;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import pt.tecnico.SDStore.handler.RelayServerHandler;
import pt.ulisboa.tecnico.sdis.store.ws.*; // classes generated from WSDL

@WebService(
    endpointInterface="pt.ulisboa.tecnico.sdis.store.ws.SDStore", 
    wsdlLocation="SD-STORE.1_1.wsdl",
    name="SDStore",
    portName="SDStoreImplPort",
    targetNamespace="urn:pt:ulisboa:tecnico:sdis:store:ws",
    serviceName="SDStore"
)
@HandlerChain(file="/handler-chain.xml")
public class SDStoreImpl implements SDStore {

private ArrayList <userDirectory> folders;
private String[] tag;
@Resource
private WebServiceContext webServiceContext;


	public SDStoreImpl(){
		folders = new ArrayList<userDirectory>();
	}
	
	public void setup() throws DocAlreadyExists_Exception{
		userDirectory dir = new userDirectory("alice");
		dir.addDoc("AAAAAAAAAA");
		dir.addDoc("aaaaaaaaaa");
		folders.add(dir);
		
		dir = new userDirectory("bruno");
		dir.addDoc("BBBBBBBBBBBBBBBBBBBB");
		folders.add(dir);
		
		folders.add(new userDirectory("carla"));
		folders.add(new userDirectory("duarte"));
		folders.add(new userDirectory("eduardo"));
	}
	
     //list user stored documents; if user does not exists, throws exception
    public List<String> listDocs(String name) throws UserDoesNotExist_Exception {
    	String user = name;
    	userDirectory folder = null;

    	for(userDirectory aux : folders)
			if(aux.getUser().equals(user)){
				folder = aux;
				return folder.getDocsNames();
			}

		
		UserDoesNotExist userException = new UserDoesNotExist();
		userException.setUserId(user);
		throw new UserDoesNotExist_Exception("User does not exist", userException);			
    }
    

    //creates document for user; if user does not exists, creates user; if document already exists, throws exception
	public void createDoc(DocUserPair docUserPair) throws DocAlreadyExists_Exception {
		String user = docUserPair.getUserId();
		String doc = docUserPair.getDocumentId();
		if(user == null || doc == null || user.isEmpty() || doc.isEmpty())
			return;
		
		userDirectory folder = null;
				
		for(userDirectory aux : folders)
			if(aux.getUser().equals(user))
				folder = aux;
			
		if(folder==null){
			folder = new userDirectory(user);
			folders.add(folder);
		}
		
		//String[] tag = getFromHandler();
		folder.addDoc(doc);	
		sendToHandler();
	}

	public void store(DocUserPair docUserPair, byte[] contents)
			throws CapacityExceeded_Exception, DocDoesNotExist_Exception,
			UserDoesNotExist_Exception {

		if (contents == null)
			return;
		
    	String user = docUserPair.getUserId();
    	String docId = docUserPair.getDocumentId();
    	userDirectory folder = null;

    	String[] tag = getFromHandler();
    	for(userDirectory aux : folders)
			if(aux.getUser().equals(user)){
				folder = aux;
				folder.storeContent(docId, contents, tag);
				sendToHandler();
				return;
			}
    	
		
		UserDoesNotExist userException = new UserDoesNotExist();
		userException.setUserId(user);
		throw new UserDoesNotExist_Exception("User does not exist", userException);
			
		
	}

	public byte[] load(DocUserPair docUserPair)
			throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
		String user = docUserPair.getUserId();
		String docId = docUserPair.getDocumentId();
    	userDirectory folder = null;

    	for(userDirectory aux : folders)
			if(aux.getUser().equals(user)){
				folder = aux;
				document doc = folder.searchDoc(docId);
				sendToHandler(doc.getSeqNumber(), doc.getTagID());
				return folder.loadContent(docId);
			}

		
		UserDoesNotExist userException = new UserDoesNotExist();
		userException.setUserId(user);
		throw new UserDoesNotExist_Exception("User does not exist", userException);	
	}
	
	// get tag from SOAP message
	private String[] getFromHandler(){
        MessageContext messageContext = webServiceContext.getMessageContext();
        String propertyValue = (String) messageContext.get(RelayServerHandler.REQUEST_PROPERTY);
        String[] result = {propertyValue.split(";")[0].split(":")[1], propertyValue.split(";")[1].split(":")[1]};
        return result;
	}

	// set tag into SOAP message for client <--- necessário?
	private void sendToHandler(int newSeq, int newID){
		MessageContext messageContext = webServiceContext.getMessageContext();
        String newValue = newSeq + ";" + newID;
        messageContext.put(RelayServerHandler.RESPONSE_PROPERTY, newValue);
	}
	
	private void sendToHandler(){
		MessageContext messageContext = webServiceContext.getMessageContext();
        String newValue = "ack";
        messageContext.put(RelayServerHandler.RESPONSE_PROPERTY, newValue);
	}
	
	public void setcontext(WebServiceContext newService){
		webServiceContext = newService;
	}
	
	public WebServiceContext getcontext(){
		return webServiceContext;
	}

}
