package pt.tecnico;

import java.util.List;
import java.util.Map;

import javax.xml.registry.JAXRException;

import pt.tecnico.handler.SecurityHandler;
import pt.ulisboa.tecnico.sdis.store.ws.CapacityExceeded_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocDoesNotExist_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocUserPair;
import pt.ulisboa.tecnico.sdis.store.ws.InvalidArgument;
import pt.ulisboa.tecnico.sdis.store.ws.InvalidArgument_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.UnauthorizedOperation;
import pt.ulisboa.tecnico.sdis.store.ws.UnauthorizedOperation_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.UserDoesNotExist_Exception;

public class StoreClient {

    private static ReplicationFrontEnd frontEnd;
    private static FrontEndSDStore frontSDStore;
    private Map<String, Object> requestContext;
    private static StoreClient instance;
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
        frontEnd = new ReplicationFrontEnd(gen);
        genericClient = frontEnd.getGenericClient();
        frontSDStore = new FrontEndSDStore(ClientMain.UDDI_URL, ClientMain.STORE_NAME, 1);
    }

    public void createDoc(DocUserPair docUserPair) throws DocAlreadyExists_Exception, UnauthorizedOperation_Exception,
                                                  InvalidArgument_Exception {
        if (docUserPair == null || docUserPair.getDocumentId() == null || docUserPair.getDocumentId().isEmpty()
                || docUserPair.getUserId() == null || docUserPair.getUserId().isEmpty())
            throw new InvalidArgument_Exception("The argument is either empty or null", new InvalidArgument());

        String username = docUserPair.getUserId();
        if (loadContext(username) != 0) {
            UnauthorizedOperation op = new UnauthorizedOperation();
            op.setUserId(username);
            throw new UnauthorizedOperation_Exception("No authentication data", op);
        }

        System.out.println("Requesting document \"" + docUserPair.getDocumentId() + "\" creation for " + username);
        frontEnd.createDoc(docUserPair);
    }

    public List<String> listDocs(String userId) throws UserDoesNotExist_Exception, UnauthorizedOperation_Exception,
                                               InvalidArgument_Exception {
        if (userId == null || userId.isEmpty())
            throw new InvalidArgument_Exception("The argument is either empty or null", new InvalidArgument());

        if (loadContext(userId) != 0) {
            UnauthorizedOperation op = new UnauthorizedOperation();
            op.setUserId(userId);
            throw new UnauthorizedOperation_Exception("No authentication data", op);
        }
        loadContext(userId);
        System.out.println("Requesting " + userId + "'s documents");
        return frontEnd.listDocs(userId);
    }

    public void store(String username, String docId, byte[] contents) throws CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception, DocAlreadyExists_Exception, UnauthorizedOperation_Exception, InvalidArgument_Exception {
        DocUserPair pair = new DocUserPair();
        pair.setUserId(username);
        pair.setDocumentId(docId);
    //    loadContext(username);
        try{
            frontSDStore.store(pair, contents);
        } catch(DocDoesNotExist_Exception e){
            createDoc(pair);
            frontSDStore.store(pair, contents);
        }
    }

    public byte[] load(String username, String docId) throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
  //      loadContext(username);
        DocUserPair pair = new DocUserPair();
        pair.setUserId(username);
        pair.setDocumentId(docId);
        return frontSDStore.load(pair);
    }

    int loadContext(String username) {
        String sessionKey = genericClient.sessionKeys.get(username);
        String ticket = genericClient.tickets.get(username);

        if (ticket == null || ticket == "" || sessionKey == "" || sessionKey == null)
            return -1;

        frontEnd.putRequestContext(SecurityHandler.SESSION_KEY, sessionKey);
        frontEnd.putRequestContext(SecurityHandler.TICKET, ticket);
        frontEnd.putRequestContext(SecurityHandler.CLIENT, username);
        frontEnd.putRequestContext(SecurityHandler.TYPE, "SDID");
        return 0;
    }
}
