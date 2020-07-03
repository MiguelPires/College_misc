package pt.tecnico;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import javax.xml.registry.JAXRException;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Response;

import pt.tecnico.handler.SecurityHandler;
import pt.ulisboa.tecnico.sdis.store.ws.CapacityExceeded_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.CreateDocResponse;
import pt.ulisboa.tecnico.sdis.store.ws.DocAlreadyExists;
import pt.ulisboa.tecnico.sdis.store.ws.DocAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocDoesNotExist_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocUserPair;
import pt.ulisboa.tecnico.sdis.store.ws.InvalidArgument;
import pt.ulisboa.tecnico.sdis.store.ws.InvalidArgument_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.ListDocsResponse;
import pt.ulisboa.tecnico.sdis.store.ws.SDStore;
import pt.ulisboa.tecnico.sdis.store.ws.SDStore_Service;
import pt.ulisboa.tecnico.sdis.store.ws.UnauthorizedOperation;
import pt.ulisboa.tecnico.sdis.store.ws.UnauthorizedOperation_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.UserDoesNotExist_Exception;
import uddi.UDDINaming;

public class ReplicationFrontEnd {

    private static final int REPLICAS_NUMBER = 3;
    
    /* The following equations must ALWAYS be true:
    *
    *   RT + WT > REPLICAS_NUMBER
    *   2 * WT > REPLICAS_NUMBER
    *
    */
    
    private static final int RT = REPLICAS_NUMBER/2 + 1, WT = REPLICAS_NUMBER/2 + 1;

    private SDStore[] storeServer;
    private Map<String, Object>[] requestContext;
    private Client genericClient;

    private List<List<String>> docLists;
    private int acks;
    private boolean finished;
    private DocAlreadyExists_Exception alreadyExistsException;
    private UnauthorizedOperation_Exception unauthorizedException;
    private DocUserPair docPair;
    private UnauthorizedOperation unauthorizedOperationFault;

    public ReplicationFrontEnd(Client gen) throws JAXRException {
        genericClient = gen;

        UDDINaming uddiNaming = new UDDINaming(ClientMain.UDDI_URL);

        storeServer = new SDStore[REPLICAS_NUMBER];
        requestContext = (Map<String, Object>[]) new Map[REPLICAS_NUMBER];
        String storeAddress;

        for (int i = 0; i < REPLICAS_NUMBER; i++) {
            storeAddress = uddiNaming.lookup(ClientMain.STORE_NAME + "-" + i);
            if (storeAddress == null) {
                System.out.println("The server \"" + ClientMain.STORE_NAME + "-" + i + "\" wasn't found");
                continue;
            } else {
                System.out.println("The address \"" + storeAddress + "\" was found");
            }

            SDStore_Service storeService = new SDStore_Service();
            storeServer[i] = storeService.getSDStoreImplPort();

            BindingProvider storeBindingProvider = (BindingProvider) storeServer[i];
            requestContext[i] = storeBindingProvider.getRequestContext();
            requestContext[i].put(ENDPOINT_ADDRESS_PROPERTY, storeAddress);
        }
    }

    public Map<String, Object>[] getRequestContext() {
        return requestContext;
    }

    public void putRequestContext(String key, String value) {
        for (int i = 0; i < REPLICAS_NUMBER; i++) {
            requestContext[i].put(key, value);
        }
    }

    public Client getGenericClient() {
        return genericClient;
    }

    private boolean reachedWT(){
        return acks >= WT;
    }
    
    private boolean reachedRT(){
        return acks >= RT;
    }
    
    public void createDoc(DocUserPair docUserPair) throws DocAlreadyExists_Exception, UnauthorizedOperation_Exception {
        acks = 0;
        alreadyExistsException = null;
        unauthorizedException = null;
        docPair = docUserPair;
        
        for (int i = 0; i < REPLICAS_NUMBER; i++) {
            if ((ClientMain.REPL_DEMO && i <= REPLICAS_NUMBER / 2) || !ClientMain.REPL_DEMO) {
                requestContext[i].put(SecurityHandler.TYPE, "SDID");

                storeServer[i].createDocAsync(docUserPair, new AsyncHandler<CreateDocResponse>() {
                    public void handleResponse(Response<CreateDocResponse> response) {
                        System.out.println();
                        ++acks;
                        try {
                            response.get();
                        } catch (ExecutionException e) {
                            if (e.getCause().toString().contains("DocAlreadyExists")) {
                                DocAlreadyExists fault = new DocAlreadyExists();
                                fault.setDocId(docPair.getDocumentId());
                                alreadyExistsException = new DocAlreadyExists_Exception("Document " + docPair.getDocumentId()
                                        + " already exists", fault);
                            } else if (e.getCause().toString().contains("UnauthorizedOperation")) {
                                UnauthorizedOperation fault = new UnauthorizedOperation();
                                fault.setUserId(docPair.getUserId());
                                unauthorizedException = new UnauthorizedOperation_Exception("Unauthorized operation", fault);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } 
                    }
                });
            }
        }
        // wait until Q acks arrive 
        while (!reachedWT()) {
            try {
                if (unauthorizedException != null)
                    throw unauthorizedException;
                else if (alreadyExistsException != null)
                    throw alreadyExistsException;

                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> listDocs(String userId) throws UserDoesNotExist_Exception, UnauthorizedOperation_Exception {
        acks = 0;
        finished = false;
        docLists = Collections.synchronizedList(new ArrayList<List<String>>());

        for (int i = 0; i < REPLICAS_NUMBER; i++) {
            storeServer[i].listDocsAsync(userId, new AsyncHandler<ListDocsResponse>() {
                public void handleResponse(Response<ListDocsResponse> response) {
                    synchronized (this) {
                        try {
                            if (finished) // ignore responses after the timeout
                                return;

                            ArrayList<String> docList = new ArrayList<String>();
                            String address = (String) response.getContext().get("javax.xml.ws.service.endpoint.address");
                            String endpointNumber = address.substring(address.length() - 1, address.length());

                            for (String doc : response.get().getDocumentId()) {
                                docList.add(doc);
                            }
                            docList.add(endpointNumber);
                            docLists.add(docList);

                            ++acks;
                        } catch(ExecutionException e) {
                            if (e.getCause().toString().contains("UserDoesNotExist")) {
                                // replica has no documents, append the endpoint number only

                                String address = (String) response.getContext().get("javax.xml.ws.service.endpoint.address");
                                String endpointNumber = address.substring(address.length() - 1, address.length());
                                ArrayList<String> docList = new ArrayList<String>();
                                docList.add(endpointNumber);
                                docLists.add(docList);
                            } else if (e.getCause().toString().contains("UnauthorizedOperation")) {
                                UnauthorizedOperation fault = new UnauthorizedOperation();
                                fault.setUserId(docPair.getUserId());
                                unauthorizedException = new UnauthorizedOperation_Exception("Unauthorized operation", fault);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        // wait until Q acks arrive 
        while (!reachedRT()) {
            try {
                if (unauthorizedException != null)
                    throw unauthorizedException;
                
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



        if (acks < REPLICAS_NUMBER) {
            System.out.print("Waiting for remaining responses");

            // wait a second for the remaining replicas and assume the request were lost after that
            while (!finished) {
                try {
                    Thread.sleep(1000);
                    finished = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        ArrayList<String> answers = new ArrayList<String>();
        for (List<String> docList : docLists) {
            answers.add(docList.get(docList.size() - 1));
        }

        // check which replicas didn't respond to the request
        for (int i = 0; i < REPLICAS_NUMBER; ++i) {
            String num = (new Integer(i)).toString();
            if (!answers.contains(num)) {
                List<String> missing = new ArrayList<String>();
                missing.add(num);
                docLists.add(missing);
            }
        }

        // computes the superset of all the replica's documents
        Set<String> totalSet = new TreeSet<String>();
        for (List<String> docs : docLists) {
            for (int i = 0; i < docs.size() - 1; ++i)
                totalSet.add(docs.get(i));
        }

        // writes all the missing documents to the replicas where they're missing 
        try {
            writeBack(totalSet, userId);
        } catch (DocAlreadyExists_Exception e) {
            e.printStackTrace();
        } catch (InvalidArgument_Exception e) {
            e.printStackTrace();
        }

        List<String> retList = new ArrayList<String>();
        retList.addAll(totalSet);
        return retList;
    }

    private void writeBack(Set<String> totalSet, String userId) throws UnauthorizedOperation_Exception, DocAlreadyExists_Exception,
                                                               InvalidArgument_Exception {
        ClientMain.REPL_DEMO = false; // ends the replication demo

        for (String doc : totalSet) {
            for (List<String> docs : docLists) {

                List<String> docsOnly = docs.subList(0, docs.size() - 1);
                if (!docsOnly.contains(doc)) {
                    DocUserPair newDoc = new DocUserPair();
                    newDoc.setUserId(userId);
                    newDoc.setDocumentId(doc);

                    String addressNumber = docs.get(docs.size() - 1);
                    int replicaNumber = Integer.parseInt(addressNumber);
                    System.out.println("Write back for replica " + addressNumber);

                    String sessionKey = genericClient.sessionKeys.get(userId);
                    String ticket = genericClient.tickets.get(userId);

                    if (ticket == null || ticket == "" || sessionKey == "" || sessionKey == null)
                        throw new InvalidArgument_Exception("Write back failed", new InvalidArgument());

                    requestContext[replicaNumber].put(SecurityHandler.SESSION_KEY, sessionKey);
                    requestContext[replicaNumber].put(SecurityHandler.TICKET, ticket);
                    requestContext[replicaNumber].put(SecurityHandler.CLIENT, userId);
                    requestContext[replicaNumber].put(SecurityHandler.TYPE, "SDID");

                    storeServer[replicaNumber].createDoc(newDoc);
                }

            }
        }
    }

    public void store(DocUserPair docUserPair, byte[] contents) throws CapacityExceeded_Exception, DocDoesNotExist_Exception,
                                                               UserDoesNotExist_Exception {
        // TODO Auto-generated method stub

    }

    public byte[] load(DocUserPair docUserPair) throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
