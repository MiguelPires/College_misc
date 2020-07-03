package pt.tecnico.bubbledocs.service.remote;

import javax.xml.registry.JAXRException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import pt.tecnico.Client;
import pt.tecnico.bubbledocs.exception.CannotLoadDocumentException;
import pt.tecnico.bubbledocs.exception.CannotStoreDocumentException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.ulisboa.tecnico.sdis.store.ws.CapacityExceeded_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocDoesNotExist_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.InvalidArgument_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.UnauthorizedOperation_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.UserDoesNotExist_Exception;

public class StoreRemoteServices {

    //duvidas excepções
    public void storeDocument(String username, String docName, byte[] document) {
        try {
            Client.getInstanceStore().store(username, docName, document);
        } catch (JAXRException e) {
            throw new RemoteInvocationException();
        } catch (CapacityExceeded_Exception e) {
            throw new CannotStoreDocumentException();
        } catch (DocDoesNotExist_Exception e) {
            throw new CannotStoreDocumentException();
        } catch (UserDoesNotExist_Exception e) {
            throw new CannotStoreDocumentException();
        } catch (DocAlreadyExists_Exception e) {
            throw new CannotStoreDocumentException();
        } catch (UnauthorizedOperation_Exception e) {
            System.out.println("Unauthorized operation - Store remote");
        } catch (InvalidArgument_Exception e) {
            System.out.println("invalid arg - Store remote");
        } catch (TransformerFactoryConfigurationError e) {
            System.out.println("transformer error - Store remote");
        }
    }

    public byte[] loadDocument(String username, String docName) {

        try {
            return Client.getInstanceStore().load(username, docName);
        } catch (JAXRException e) {
            throw new RemoteInvocationException();
        } catch (DocDoesNotExist_Exception e) {
            throw new CannotLoadDocumentException();
        } catch (UserDoesNotExist_Exception e) {
            throw new CannotLoadDocumentException();
        }
    }
}
