package pt.ulisboa.tecnico.sdis.store.ws.impl;

import java.util.HashMap;
import java.util.List;

import javax.activity.InvalidActivityException;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import pt.ulisboa.tecnico.sdis.store.ws.CapacityExceeded_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocAlreadyExists;
import pt.ulisboa.tecnico.sdis.store.ws.DocAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocDoesNotExist_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocUserPair;
import pt.ulisboa.tecnico.sdis.store.ws.InvalidArgument;
import pt.ulisboa.tecnico.sdis.store.ws.InvalidArgument_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.SDStore;
import pt.ulisboa.tecnico.sdis.store.ws.UnauthorizedOperation;
import pt.ulisboa.tecnico.sdis.store.ws.UnauthorizedOperation_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.UserDoesNotExist;
import pt.ulisboa.tecnico.sdis.store.ws.UserDoesNotExist_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.handler.SecurityHandler;

@WebService(endpointInterface = "pt.ulisboa.tecnico.sdis.store.ws.SDStore", wsdlLocation = "SD-STORE.1_1.wsdl", name = "SDStore", portName = "SDStoreImplPort", targetNamespace = "urn:pt:ulisboa:tecnico:sdis:store:ws", serviceName = "SDStore")
@HandlerChain(file = "/handler-chain.xml")
public class StoreImpl implements SDStore {

    private String key;
    private String serviceName;

    @Resource
    public WebServiceContext webServiceContext;

    public StoreImpl(String key, String serviceName) {
        this.key = key;
        this.serviceName = serviceName;
    }


    public HashMap<String, DocumentRepository> userRepositories = new HashMap<String, DocumentRepository>();

    /*
     * From WSDL: <!-- Creates a new document in the provided user's repository.
     * In case this is the first operation on that user, a new repository is
     * created for the new user. Faults: a document already exists with the same
     * id -->
     */
    public void createDoc(DocUserPair docUserPair) throws DocAlreadyExists_Exception, UnauthorizedOperation_Exception,
                                                  InvalidArgument_Exception {
        String userId = docUserPair.getUserId();

        if (docUserPair == null || userId == null || userId.isEmpty() || docUserPair.getDocumentId() == null
                || docUserPair.getDocumentId().isEmpty())
            throw new InvalidArgument_Exception("Argument is invalid", new InvalidArgument());

        else {
            MessageContext smc = webServiceContext.getMessageContext();
            String ticketClient = (String) smc.get(SecurityHandler.CLIENT);
            if (!ticketClient.equals(userId)) {

                UnauthorizedOperation fault = new UnauthorizedOperation();
                fault.setUserId(userId);
                throw new UnauthorizedOperation_Exception("The client name in the ticket doesn't match the request", fault);
            }

            System.out.println(serviceName + ": Creating document \"" + docUserPair.getDocumentId() + "\" for " + userId);
            DocumentRepository rep = userRepositories.get(docUserPair.getUserId());

            if (rep == null) {
                rep = new DocumentRepository();
                userRepositories.put(docUserPair.getUserId(), rep);
            }

            if (rep.addNewDocument(docUserPair.getDocumentId()) == false) {
                DocAlreadyExists faultInfo = new DocAlreadyExists();
                faultInfo.setDocId(docUserPair.getDocumentId());
                throw new DocAlreadyExists_Exception("Document already exists", faultInfo);
            }
        }
    }

    /*
     * From WSDL: <!-- Lists the document ids of the user's repository. Faults:
     * user does not exist -->
     */
    public List<String> listDocs(String userId) throws UserDoesNotExist_Exception, UnauthorizedOperation_Exception,
                                               InvalidArgument_Exception {

        if (userId == null || userId.isEmpty())
            throw new InvalidArgument_Exception("Argument is invalid", new InvalidArgument());

        else if (userRepositories.get(userId) == null) {
            UserDoesNotExist faultInfo = new UserDoesNotExist();
            faultInfo.setUserId(userId);
            System.out.println(serviceName+": No documents stored for "+userId);
            throw new UserDoesNotExist_Exception("User does not exist **", faultInfo);
        }

        MessageContext smc = webServiceContext.getMessageContext();
        String ticketClient = (String) smc.get(SecurityHandler.CLIENT);

        if (!ticketClient.equals(userId)) {
            UnauthorizedOperation fault = new UnauthorizedOperation();
            fault.setUserId(userId);
            throw new UnauthorizedOperation_Exception("The client name in the ticket doesn't match the request", fault);
        }

        System.out.println(serviceName+": Listing " + userId + "'s documents");
        DocumentRepository rep = userRepositories.get(userId);
        System.out.println(rep.listDocs(userId));
        return rep.listDocs(userId);
        
    }

    /*
     * From WSDL: <!-- Replaces the entire contents of the document by the
     * contents provided as argument. Faults: document does not exist, user does
     * not exist, repository capacity is exceeded. -->
     */
    public void store(DocUserPair docUserPair, byte[] newContents) throws CapacityExceeded_Exception, DocDoesNotExist_Exception,
                                                                  UserDoesNotExist_Exception {

        throw new UnsupportedOperationException("Not implemented in minimal version!");
    }

    /*
     * From WSDL: <!-- Returns the current contents of the document. Fault: user
     * or document do not exist -->
     */
    public byte[] load(DocUserPair docUserPair) throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {

        throw new UnsupportedOperationException("Not implemented in minimal version!");
    }

    // for testing
    void reset() {
        userRepositories.clear();
        // as specified in:
        // http://disciplinas.tecnico.ulisboa.pt/leic-sod/2014-2015/labs/proj/test.html
        {
            DocumentRepository rep = new DocumentRepository();
            userRepositories.put("alice", rep);
        }
        {
            DocumentRepository rep = new DocumentRepository();
            userRepositories.put("bruno", rep);
        }
        {
            DocumentRepository rep = new DocumentRepository();
            userRepositories.put("carla", rep);
        }
        {
            DocumentRepository rep = new DocumentRepository();
            userRepositories.put("dimas", rep);
        }
    }
}
