package pt.tecnico.SDStore;

import java.util.*;
import java.security.*;

import javax.crypto.*;
import javax.annotation.Resource;
import javax.jws.*;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import java.util.Base64;

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
	private String serverKey;
	@Resource
	private WebServiceContext webServiceContext;
	private Map<String, byte[]> digestMap = new HashMap<String, byte[]>();
	
	public SecureSDStore(SDStoreImpl server, String key) throws NoSuchAlgorithmException {
		this.server = server;
		this.key = generateKey("AES");
		this.serverKey=key;
	}
	
     //list user stored documents; if user does not exists, throws exception
    public List<String> listDocs(String name) throws UserDoesNotExist_Exception, UnauthorizedOperation_Exception, InvalidArgument_Exception {
    	server.setcontext(webServiceContext);
    	List<String> result = server.listDocs(name);
    	webServiceContext = server.getcontext();
    	return result;	
    }
    

    //creates document for user; if user does not exists, creates user; if document already exists, throws exception
	public void createDoc(DocUserPair docUserPair) throws DocAlreadyExists_Exception, UnauthorizedOperation_Exception, InvalidArgument_Exception {
		server.setcontext(webServiceContext);
		server.createDoc(docUserPair);	
		webServiceContext = server.getcontext();
	}

	public void store(DocUserPair docUserPair, byte[] contents) throws CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception {
		byte[]  cipheredContent=null;
		server.setcontext(webServiceContext);
		//if(contents!=null)
			//cipheredContent = cipher(docUserPair.getDocumentId(), contents);
		
		
		server.store(docUserPair, contents);
		webServiceContext = server.getcontext();
	}

	public byte[] load(DocUserPair docUserPair) throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
		server.setcontext(webServiceContext);
		byte[] cipheredDoc = server.load(docUserPair);
		//byte[] decipheredDoc = null;
		//if(cipheredDoc!=null)
			//decipheredDoc = decipher(docUserPair.getDocumentId(), cipheredDoc);
		webServiceContext = server.getcontext();
		return cipheredDoc;
	}

	// generate key with the AES (Rijndael) algorithm
	public Key generateKey(String algorithm) throws NoSuchAlgorithmException {
	       
		KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
		keyGen.init(128);
		Key key = keyGen.generateKey();
		return key;
	}
	
	public byte[] cipher(String docId, byte[] message) {
		try{
		// Generate MAC: generate message digest and cipher
		
			// convert message and key to string and concatenate both
			String convertedMessage = printBase64Binary(message);
			String convertedKey = Base64.getEncoder().encodeToString(key.getEncoded());
			byte[] result = parseBase64Binary(convertedMessage+convertedKey);

			// get a message digest object using the MD5 algorithm and create the digest
        	MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        	messageDigest.update(result); 
        	byte[] digest = messageDigest.digest();
        	
        	// store the digest to compare later
	       	digestMap.put(docId, digest);
						
			byte[] cipherMessage = cipherMessage("Encrypt", message);
						
        	return cipherMessage;
			 } catch(Exception e){
				e.printStackTrace();
				return null;
			 }
		}
			 
	// returns the signed decrypted message
	public byte[] decipher(String docId, byte[] message) {  
		try{		
		// Decrypt and verify MAC
		
			byte[] decipheredMessage = cipherMessage("Decrypt", message); 
			       
			// convert message and key to string and concatenate both
	       	String convertedMessage = printBase64Binary(decipheredMessage);
	      	String convertedKey = Base64.getEncoder().encodeToString(key.getEncoded());
			byte[] result = parseBase64Binary(convertedMessage+convertedKey);

			// get a message digest object using the MD5 algorithm and create the digest
        	MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        	messageDigest.update(result); 
			byte[] newDigest = messageDigest.digest();
			
	       	// find original message digest
	       	byte [] oldDigest = null;
	       	for (Map.Entry<String, byte[]> entry : digestMap.entrySet()) {
	       		if (entry.getKey().equals(docId)) {
	   				oldDigest = entry.getValue();
	    			}
	    		}
			
			// compare both digests to verify if the message has been modified
			if (oldDigest == null || (newDigest.length != oldDigest.length))  
         		throw new Exception();
		       		
			for (int i = 0; i < newDigest.length; i++) {
			    if (newDigest[i] != oldDigest[i]) 
			        throw new Exception();    		
			    }
			return decipheredMessage; 
			
			} catch(Exception e){
			    System.out.printf("The content has been modified!", e);
			 	return null;
				}
			}	
	
	public byte[] cipherMessage(String mode, byte[] message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		// get a AES cipher object
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
								
		// decrypt the message using the same key
		if(mode.equals("Decrypt"))
			cipher.init(Cipher.DECRYPT_MODE, key);
		else
			cipher.init(Cipher.ENCRYPT_MODE, key);
		
		return cipher.doFinal(message); 
	}
			
}