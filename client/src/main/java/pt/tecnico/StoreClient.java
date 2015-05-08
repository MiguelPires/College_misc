package pt.tecnico;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.List;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.store.ws.CapacityExceeded_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocDoesNotExist_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocUserPair;
import pt.ulisboa.tecnico.sdis.store.ws.SDStore;
import pt.ulisboa.tecnico.sdis.store.ws.SDStore_Service;
import pt.ulisboa.tecnico.sdis.store.ws.UserDoesNotExist_Exception;
import uddi.UDDINaming;

public class StoreClient implements SDStore {

    private SDStore storeServer;
    private Map<String, Object> requestContext;
    private static SDStore instance;

    public Map<String, Object> getRequestContext() {
        return requestContext;
    }

    public static StoreClient getInstance(String uddiURL, String serverName) throws JAXRException {
        if (instance == null)
            instance = new StoreClient(uddiURL, serverName);
        return (StoreClient) instance;
    }

    private StoreClient(String uddiURL, String serverName) throws JAXRException {
        UDDINaming uddiNaming = new UDDINaming(uddiURL);

        String storeAddress = uddiNaming.lookup(serverName);
        if (storeAddress == null) {
            System.out.println("The server \"" + serverName + "\" wasn't found");
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

    public void createDoc(DocUserPair docUserPair) throws DocAlreadyExists_Exception {
        storeServer.createDoc(docUserPair);
    }

    public List<String> listDocs(String userId) throws UserDoesNotExist_Exception {
        return storeServer.listDocs(userId);
    }

    public void store(DocUserPair docUserPair, byte[] contents) throws CapacityExceeded_Exception, DocDoesNotExist_Exception,
                                                               UserDoesNotExist_Exception {
        storeServer.store(docUserPair, contents);
    }

    public byte[] load(DocUserPair docUserPair) throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
        return storeServer.load(docUserPair);
    }

}
