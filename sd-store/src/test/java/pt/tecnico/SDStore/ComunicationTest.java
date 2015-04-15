package pt.tecnico.SDStore;

import pt.tecnico.ws.uddi.UDDINaming;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.registry.JAXRException;
import org.junit.*;

import static org.junit.Assert.*;
import mockit.*;


/**
 *  Test Communication for JUDDI
 */
public class ComunicationTest {

String uddiURL = "http://localhost:8081";
String name = "SD-Store";
String url = "http://localhost:8080/store-ws/endpoint";

	
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
    
}
