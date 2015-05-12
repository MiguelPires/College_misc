package pt.tecnico.bubbledocs.service.remote;

import javax.xml.registry.JAXRException;
import pt.tecnico.bubbledocs.exception.CannotLoadDocumentException;
import pt.tecnico.bubbledocs.exception.CannotStoreDocumentException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.SDStore.StoreClient;

public class StoreRemoteServices {

    String uddiURL = "http://localhost:8081";
    String name = "SD-STORE";

    public StoreRemoteServices(){}

    public void storeDocument(String username, String docName, byte[] document) {
    	try{
    		StoreClient client = new StoreClient(uddiURL, name);

    		client.store(username, docName, document);
        } catch (JAXRException e) {
            throw new RemoteInvocationException();
        } catch(Exception e){
        	throw new CannotStoreDocumentException();
        }
    }

    public byte[] loadDocument(String username, String docName) {
    	byte[] content = null;
    	try{
    		StoreClient client = new StoreClient(uddiURL, name);
    		
        	content = client.load(username, docName);
        } catch (JAXRException e) {
            throw new RemoteInvocationException();
        } catch(Exception e){
        	throw new CannotLoadDocumentException();
        }

        return content;
    }
}
