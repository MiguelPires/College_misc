package pt.tecnico.SDStore;

import pt.tecnico.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.store.ws.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.registry.JAXRException;

import org.junit.*;

import static org.junit.Assert.*;
import mockit.*;

import javax.xml.ws.*;

import com.sun.xml.ws.api.EndpointAddress;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;


/**
 *  Test Communication for JUDDI
 */
public class ComunicationTest {

String uddiURL = "http://localhost:8081";
String name = "SD-Store";
String url = "http://localhost:8082/store-ws/endpoint";

	
    //Tests UDDI server - publishes and checks if it's there
    @Test
    public void UDDIsuccess() throws JAXRException {
    	new MockUp<UDDINaming>() {
            @Mock
            private boolean publish(String orgName, String url) throws JAXRException {
            	return true;
            };
            @Mock
            private Collection<String> queryAll(String orgName) throws JAXRException {
            	List<String> result = new ArrayList<String>();
            	result.add(url);
            	return result;
            };
        };
        
        UDDINaming uddiNaming = new UDDINaming(uddiURL);
        uddiNaming.bind(name, url);

        String endpointAddress = uddiNaming.lookup(name);
        
        assertEquals(url, endpointAddress);
    }
    
    //Tests arguments to new uddiNaming (only accepts HTTP)
    @Test(expected=IllegalArgumentException.class)
    public void UDDIInvalidArguments() throws JAXRException{

    	String newuddiURL="www://localhost:8080/store-ws/endpoint";
    	new UDDINaming(newuddiURL);
    }
    
    //Connect to uddi while uddi server is offline
    @Test(expected=JAXRException.class)
    public void connectUDDIOffline() throws JAXRException{
    	UDDINaming uddi = new UDDINaming(uddiURL);
    	uddi.bind(name, url);
    }
    
    //Tests if Endpoint object is creating and publishing properly
    @Test
    public void testEndpoint(){
    	 Endpoint endpoint = null;
         SDStoreImpl Store=new SDStoreImpl();
         endpoint = Endpoint.create(Store);
         endpoint.publish(url);
         
         assertEquals(true, endpoint.isPublished());
         assertEquals(Store.getClass(), endpoint.getImplementor().getClass()); 
         endpoint.stop();
         assertEquals(false, endpoint.isPublished());
    }
    
    //Tests if after an createDoc from client (port) affects the server (Store)
    @Test
    public void testClientServer() throws DocAlreadyExists_Exception, UserDoesNotExist_Exception {
    	Endpoint endpoint = null;
    	SDStoreImpl Store=new SDStoreImpl();
        endpoint = Endpoint.create(Store);
        endpoint.publish(url);
 
    	SDStore_Service service = new SDStore_Service();
    	SDStore port = service.getSDStoreImplPort();
    
    	BindingProvider bindingProvider = (BindingProvider) port;
    	Map<String, Object> requestContext = bindingProvider.getRequestContext();
    	requestContext.put(ENDPOINT_ADDRESS_PROPERTY, url);

    	new MockUp<SDStoreImpl>() {
    	 @Mock
         private void sendToHandler(){
    		 ;
     		}
         @Mock
         private String[] getFromHandler(){
        	 String[] ar = {"1","2"};
             return ar;
     		}
    	};
    	
    	DocUserPair pair = new DocUserPair();
    	String doc = "docx";
    	pair.setUserId("user");
        pair.setDocumentId(doc);        
    	port.createDoc(pair); // client operation
    	
		assertEquals(Store.listDocs("user").size(), 1);

    }
}
