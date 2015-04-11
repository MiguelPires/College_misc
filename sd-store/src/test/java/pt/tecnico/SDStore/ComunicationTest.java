package pt.tecnico.SDStore;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import pt.tecnico.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.store.ws.*;

import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.*;

import com.sun.xml.ws.api.EndpointAddress;

import org.junit.*;
import static org.junit.Assert.*;


/**
 *  Test Communication for JUDDI
 */
public class ComunicationTest {

String uddiURL = "http://localhost:8081";
String name = "SDStore";
String url = "http://localhost:8080/store-ws/endpoint";

	
    //Tests UDDI server - publishes and checks if it's there
    @Test
    public void UDDItest() throws JAXRException {
     
        UDDINaming uddiNaming = new UDDINaming(uddiURL);
        uddiNaming.rebind(name, url);

        String endpointAddress = uddiNaming.lookup(name);
 
        if (uddiNaming != null)
            uddiNaming.unbind(name);
        
        assertEquals(url, endpointAddress);
    }
    
}
