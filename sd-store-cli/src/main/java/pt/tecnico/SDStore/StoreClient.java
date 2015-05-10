package pt.tecnico;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import pt.ulisboa.tecnico.sdis.store.ws.SDStore_Service;
import java.util.Map;

import pt.tecnico.SDStore.FrontEndSDStore;
import pt.tecnico.ws.uddi.UDDINaming;

public class StoreClient implements FrontEndStore{

	private FrontEndSDStore storeServer;
	private Map<String, Object> requestContext;
    private static FrontEndSDStore instance;

    public Map<String, Object> getRequestContext() {
        return requestContext;
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
