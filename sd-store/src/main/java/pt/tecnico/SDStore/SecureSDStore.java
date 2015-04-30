package pt.tecnico.SDStore;

import java.util.List;
import javax.jws.*;
import java.security.*;
import javax.crypto.*;

import pt.ulisboa.tecnico.sdis.store.ws.*; // classes generated from WSDL

@WebService(
	    endpointInterface="pt.ulisboa.tecnico.sdis.store.ws.SDStore", 
	    wsdlLocation="SD-STORE.1_1.wsdl",
	    name="SDStore",
	    portName="SDStoreImplPort",
	    targetNamespace="urn:pt:ulisboa:tecnico:sdis:store:ws",
	    serviceName="SDStore"
	)

public class SecureSDStore implements SDStore {

	private SDStoreImpl server;
	private Key key;
	 
	public SecureSDStore(SDStoreImpl server){
		this.server = server;
		
		try {
			//Generates the key to encrypt the data
			KeyGenerator keyGen = KeyGenerator.getInstance("AES"); 
		    keyGen.init(128);
		    key = keyGen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
     //list user stored documents; if user does not exists, throws exception
    public List<String> listDocs(String name) throws UserDoesNotExist_Exception {
    	return server.listDocs(name);	
    }
    

    //creates document for user; if user does not exists, creates user; if document already exists, throws exception
	public void createDoc(DocUserPair docUserPair) throws DocAlreadyExists_Exception {
		server.createDoc(docUserPair);	
	}

	public void store(DocUserPair docUserPair, byte[] contents) throws CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception {
		byte[]  cipheredContent = cipher(contents);
		server.store(docUserPair, cipheredContent);
		
	}

	public byte[] load(DocUserPair docUserPair) throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
		byte[] message = server.load(docUserPair);
		byte[] ret = decipher(message);
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
