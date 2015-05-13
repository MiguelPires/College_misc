package pt.tecnico.SDStore;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.jws.*;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import pt.tecnico.SDStore.handler.StoreServerHandler;
import pt.ulisboa.tecnico.sdis.store.ws.*; // classes generated from WSDL
import pt.tecnico.SDStore.handler.SecurityHandler;

@WebService(endpointInterface = "pt.ulisboa.tecnico.sdis.store.ws.SDStore", wsdlLocation = "SD-STORE.1_1.wsdl", name = "SDStore", portName = "SDStoreImplPort", targetNamespace = "urn:pt:ulisboa:tecnico:sdis:store:ws", serviceName = "SDStore")
@HandlerChain(file = "/handler-chain.xml")
public class SDStoreImpl implements SDStore {

    public HashMap<String, userDirectory> folders = new HashMap<String, userDirectory>();
    private String[] tag;
    @Resource
    private WebServiceContext webServiceContext;
    private String serviceName;

    public SDStoreImpl(String serviceName) {
        folders = new HashMap<String, userDirectory>();
        this.serviceName = serviceName;
        try {
            setup();
        } catch (DocAlreadyExists_Exception e) {
            ;
        }
    }

    public void setup() throws DocAlreadyExists_Exception {
        userDirectory dir = new userDirectory("alice");
        dir.addDoc("AAAAAAAAAA");
        dir.addDoc("aaaaaaaaaa");
        folders.put("alice",dir);

        dir = new userDirectory("bruno");
        dir.addDoc("BBBBBBBBBBBBBBBBBBBB");
        folders.put("bruno", dir);

        folders.put("carla", new userDirectory("carla"));
        folders.put("duarte", new userDirectory("duarte"));
        folders.put("eduardo", new userDirectory("eduardo"));
    }

    //list user stored documents; if user does not exists, throws exception 
    public List<String> listDocs(String userId) throws UserDoesNotExist_Exception, UnauthorizedOperation_Exception,
                                               InvalidArgument_Exception {

        if (userId == null || userId.isEmpty())
            throw new InvalidArgument_Exception("Argument is invalid", new InvalidArgument());

        else if (folders.get(userId) == null) {
            UserDoesNotExist faultInfo = new UserDoesNotExist();
            faultInfo.setUserId(userId);
            // fi.setMessage("User does not exist");
            throw new UserDoesNotExist_Exception("User does not exist **", faultInfo);
        }

        MessageContext smc = webServiceContext.getMessageContext();
        String ticketClient = (String) smc.get(SecurityHandler.CLIENT);

        if (!ticketClient.equals(userId)) {
            UnauthorizedOperation fault = new UnauthorizedOperation();
            fault.setUserId(userId);
            throw new UnauthorizedOperation_Exception("The client name in the ticket doesn't match the request", fault);
        }

        System.out.println("Listing " + userId + "'s documents");
        userDirectory dir = folders.get(userId);
        System.out.println(dir.getDocsNames());
        return dir.getDocsNames();
    }

    //creates document for user; if user does not exists, creates user; if document already exists, throws exception
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
            userDirectory dir = folders.get(docUserPair.getUserId());

            if (dir == null) {
                dir = new userDirectory(userId);
                folders.put(docUserPair.getUserId(), dir);
            }

            dir.addDoc(docUserPair.getDocumentId());
        }
    }

    public void store(DocUserPair docUserPair, byte[] contents) throws CapacityExceeded_Exception, DocDoesNotExist_Exception,
                                                               UserDoesNotExist_Exception {

        if (contents == null)
            return;

        String user = docUserPair.getUserId();
        String docId = docUserPair.getDocumentId();
        userDirectory dir = null;

        String[] tag = getFromHandler();
        
        dir = folders.get(user);
        
        if(dir==null){
            UserDoesNotExist userException = new UserDoesNotExist();
            userException.setUserId(user);
            throw new UserDoesNotExist_Exception("User does not exist", userException);
        }
        
        dir.storeContent(docId, contents, tag);
        System.out.println("Stored content for " + user);
        sendToHandler();

    }

    public byte[] load(DocUserPair docUserPair) throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
        String user = docUserPair.getUserId();
        String docId = docUserPair.getDocumentId();
        userDirectory dir = null;

        dir = folders.get(user);
        
        if(dir==null){
            UserDoesNotExist userException = new UserDoesNotExist();
            userException.setUserId(user);
            throw new UserDoesNotExist_Exception("User does not exist", userException);
        }
       
        System.out.println("Loading content for user " + user);
        document doc = dir.loadDoc(docId);
        
        System.out.println("Sending to handler: Seq Number:" + doc.getSeqNumber() + "," + "Client: " + doc.getTagID());
        sendToHandler(doc.getSeqNumber(), doc.getTagID());
        return doc.getContent();
    }


    // get tag from SOAP message
    private String[] getFromHandler() {
        MessageContext messageContext = webServiceContext.getMessageContext();
        String propertyValue = (String) messageContext.get(StoreServerHandler.REQUEST_PROPERTY);
        if(propertyValue==null){
            String[] a = { "0" , "0"};
            return a;
        }
        String[] result = { propertyValue.split(";")[0].split(":")[1], propertyValue.split(";")[1].split(":")[1] };
        return result;
    }

    // set tag into SOAP message for client
    private void sendToHandler(int newSeq, int newID) {
        MessageContext messageContext = webServiceContext.getMessageContext();
        String newValue = newSeq + ";" + newID;
        messageContext.put(StoreServerHandler.RESPONSE_PROPERTY, newValue);
        messageContext.put(StoreServerHandler.TYPE, "SDSTORE");
    }

    private void sendToHandler() {
        MessageContext messageContext = webServiceContext.getMessageContext();
        String newValue = "ack";
        messageContext.put(StoreServerHandler.RESPONSE_PROPERTY, newValue);
        messageContext.put(StoreServerHandler.TYPE, "SDSTORE");
    }

    public void setcontext(WebServiceContext newService) {
        webServiceContext = newService;
    }

    public WebServiceContext getcontext() {
        return webServiceContext;
    }

}
