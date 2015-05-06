package pt.tecnico.SDStore;

import java.util.List;
import java.security.*;

import javax.crypto.*;
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
public class SecureSDStore implements SDStore {

	private SDStoreImpl server;
	private Key key;
	@Resource
	private WebServiceContext webServiceContext;
	
	public SecureSDStore(SDStoreImpl server, Key key){
		this.server = server;
		this.key = key;
	}
	
     //list user stored documents; if user does not exists, throws exception
    public List<String> listDocs(String name) throws UserDoesNotExist_Exception {
    	server.setcontext(webServiceContext);
    	List<String> result = server.listDocs(name);
    	webServiceContext = server.getcontext();
    	return result;	
    }
    

    //creates document for user; if user does not exists, creates user; if document already exists, throws exception
	public void createDoc(DocUserPair docUserPair) throws DocAlreadyExists_Exception {
		server.setcontext(webServiceContext);
		server.createDoc(docUserPair);	
		webServiceContext = server.getcontext();
	}

	public void store(DocUserPair docUserPair, byte[] contents) throws CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception {
		server.setcontext(webServiceContext);
		byte[]  cipheredContent = cipher(contents);
		server.store(docUserPair, cipheredContent);
		webServiceContext = server.getcontext();
	}

	public byte[] load(DocUserPair docUserPair) throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
		server.setcontext(webServiceContext);
		byte[] message = server.load(docUserPair);
		byte[] ret = decipher(message);
		webServiceContext = server.getcontext();
		return ret;
	}

	public byte[] cipher(byte[] message) {
		try{
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] cipherBytes = cipher.doFinal(message);
        return cipherBytes;
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public byte[] decipher(byte[] message) {
		try{
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] newMessage = cipher.doFinal(message);
        return newMessage; 
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

}
