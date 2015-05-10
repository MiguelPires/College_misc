package pt.tecnico;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.List;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.tecnico.handler.SecurityHandler;
import pt.ulisboa.tecnico.sdis.store.ws.CapacityExceeded_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocDoesNotExist_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocUserPair;
import pt.ulisboa.tecnico.sdis.store.ws.SDStore;
import pt.ulisboa.tecnico.sdis.store.ws.SDStore_Service;
import pt.ulisboa.tecnico.sdis.store.ws.UnauthorizedOperation;
import pt.ulisboa.tecnico.sdis.store.ws.UnauthorizedOperation_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.UserDoesNotExist_Exception;
import uddi.UDDINaming;

public class StoreClient implements SDStore {

    private SDStore storeServer;
    private Map<String, Object> requestContext;
    private static SDStore instance;
    private Client genericClient;

    public Map<String, Object> getRequestContext() {
        return requestContext;
    }

    public static StoreClient getInstance(Client gen) throws JAXRException {
        if (instance == null)
            instance = new StoreClient(gen);
        return (StoreClient) instance;
    }

    private StoreClient(Client gen) throws JAXRException {
        genericClient = gen;
        UDDINaming uddiNaming = new UDDINaming(ClientMain.UDDI_URL);

        String storeAddress = uddiNaming.lookup(ClientMain.STORE_NAME);
        if (storeAddress == null) {
            System.out.println("The server \"" + ClientMain.STORE_NAME + "\" wasn't found");
            return;
        } else {
            System.out.println("The address \"" + storeAddress + "\" was found");
        }

        SDStore_Service storeService = new SDStore_Service();
        storeServer = storeService.getSDStoreImplPort();

        BindingProvider storeBindingProvider = (BindingProvider) storeServer;
        requestContext = storeBindingProvider.getRequestContext();
        requestContext.put(ENDPOINT_ADDRESS_PROPERTY, storeAddress);
    }

    public void createDoc(DocUserPair docUserPair) throws DocAlreadyExists_Exception, UnauthorizedOperation_Exception {
        String username = docUserPair.getUserId();
        if (loadContext(username) != 0) {
            UnauthorizedOperation op = new UnauthorizedOperation();
            op.setUserId(username);
            throw new UnauthorizedOperation_Exception("No authentication data", op);
        }
        
        System.out.println("Requesting document \""+docUserPair.getDocumentId()+"\" creation for "+username);
        storeServer.createDoc(docUserPair);
    }

    public List<String> listDocs(String userId) throws UserDoesNotExist_Exception, UnauthorizedOperation_Exception {
        if (loadContext(userId) != 0) {
            UnauthorizedOperation op = new UnauthorizedOperation();
            op.setUserId(userId);
            throw new UnauthorizedOperation_Exception("No authentication data", op);
        }
        loadContext(userId);
        System.out.println("Requesting "+userId+"'s documents");
        return storeServer.listDocs(userId);
    }

    public void store(DocUserPair docUserPair, byte[] contents) throws CapacityExceeded_Exception, DocDoesNotExist_Exception,
                                                               UserDoesNotExist_Exception {
        loadContext(docUserPair.getUserId());
        storeServer.store(docUserPair, contents);
    }

    public byte[] load(DocUserPair docUserPair) throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
        loadContext(docUserPair.getUserId());
        return storeServer.load(docUserPair);
    }

    private int loadContext(String username) {
        String sessionKey = genericClient.sessionKeys.get(username);
        String ticket = genericClient.tickets.get(username);
       
        if (ticket == null || ticket == "" || sessionKey == "" || sessionKey == null)
            return -1;
        
        getRequestContext().put(SecurityHandler.SESSION_KEY, sessionKey);
        getRequestContext().put(SecurityHandler.TICKET, ticket);
        getRequestContext().put(SecurityHandler.CLIENT, username);
        return 0;
    }
}
