package pt.tecnico.bubbledocs.service.remote;

import javax.naming.ServiceUnavailableException;
import javax.xml.registry.JAXRException;

import pt.tecnico.Client;
import pt.tecnico.bubbledocs.exception.CannotStoreDocumentException;
import pt.tecnico.bubbledocs.exception.InvalidUsernameException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.SpreadsheetNotFoundException;
import pt.ulisboa.tecnico.sdis.store.ws.CapacityExceeded_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocDoesNotExist_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocUserPair;
import pt.ulisboa.tecnico.sdis.store.ws.UserDoesNotExist_Exception;

public class StoreRemoteServices {
	
	//duvidas excepções
	public void storeDocument(String username, String docName, byte[] document) {
		DocUserPair pair = new DocUserPair();
		pair.setUserId(username);
		pair.setDocumentId(docName);
   	    try {
   	    	Client.getInstanceStore().store(pair, document);
   	      } catch (JAXRException e) {
   	    	  throw new RemoteInvocationException();
   	      }  catch (CapacityExceeded_Exception e) {
   	    	  throw new CannotStoreDocumentException();
   	      } catch (DocDoesNotExist_Exception e) {
   	    	  throw new CannotStoreDocumentException();
   	      } catch (UserDoesNotExist_Exception e) {
   	    	  throw new CannotStoreDocumentException();
   	      }
	}

	public byte[] loadDocument(String username, String docName) {
		DocUserPair pair = new DocUserPair();
		pair.setUserId(username);
		pair.setDocumentId(docName);
		try {
			return Client.getInstanceStore().load(pair);
		} catch (JAXRException e) {
 	    	throw new RemoteInvocationException();
 	    } catch (DocDoesNotExist_Exception e) {
			throw new SpreadsheetNotFoundException();
		} catch (UserDoesNotExist_Exception e) {
			throw new InvalidUsernameException();
		}
	}
}
