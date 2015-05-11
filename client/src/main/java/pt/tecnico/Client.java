package pt.tecnico;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import pt.ulisboa.tecnico.sdis.id.ws.AuthReqFailed_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.SDId;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.CapacityExceeded_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocDoesNotExist_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocUserPair;
import pt.ulisboa.tecnico.sdis.store.ws.SDStore;
import pt.ulisboa.tecnico.sdis.store.ws.UnauthorizedOperation_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.UserDoesNotExist_Exception;

public class Client implements SDId, SDStore {

    protected StoreClient storeClient;
    protected SDId idClient;
    protected Map<String, String> tickets;
    protected Map<String, String> sessionKeys;
    private static Client instance;

    public static Client getInstanceID() throws TransformerFactoryConfigurationError, JAXRException {
        if (instance == null)
            instance = new Client();
        return  instance;
    }
    
    Client() throws TransformerFactoryConfigurationError, JAXRException {
        idClient = IDClient.getInstance(this);
        storeClient = StoreClient.getInstance(this);
        tickets = new HashMap<String, String>();
        sessionKeys = new HashMap<String, String>();
    }

    public void createDoc(DocUserPair docUserPair) throws DocAlreadyExists_Exception, UnauthorizedOperation_Exception {
        storeClient.createDoc(docUserPair);
    }

    public List<String> listDocs(String userId) throws UserDoesNotExist_Exception, UnauthorizedOperation_Exception {
        return storeClient.listDocs(userId);
    }

    public void store(DocUserPair docUserPair, byte[] contents) throws CapacityExceeded_Exception, DocDoesNotExist_Exception,
                                                               UserDoesNotExist_Exception {
        storeClient.store(docUserPair, contents);
    }

    public byte[] load(DocUserPair docUserPair) throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
        return storeClient.load(docUserPair);
    }

    public void createUser(String userId, String emailAddress) throws EmailAlreadyExists_Exception, InvalidEmail_Exception,
                                                              InvalidUser_Exception, UserAlreadyExists_Exception {
        idClient.createUser(userId, emailAddress);
    }

    public void renewPassword(String userId) throws pt.ulisboa.tecnico.sdis.id.ws.UserDoesNotExist_Exception {
        idClient.renewPassword(userId);
    }

    public void removeUser(String userId) throws pt.ulisboa.tecnico.sdis.id.ws.UserDoesNotExist_Exception {
        idClient.removeUser(userId);
    }

    public byte[] requestAuthentication(String userId, byte[] reserved) throws AuthReqFailed_Exception {
        return idClient.requestAuthentication(userId, reserved);
    }
}
