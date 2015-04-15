package pt.tecnico.sdid;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import pt.tecnico.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.id.ws.SDId;
import pt.ulisboa.tecnico.sdis.id.ws.SDId_Service;

public class SDIdServiceTest {

    // for setup
    public static String serverName = System.getProperty("ws.name");
    public static String uddiURL = System.getProperty("uddi.url");
    public String serverURL = System.getProperty("ws.url");
    
    // for the asserts
    public static SDId server;   
    public static String endpointAddress;
    
    @BeforeClass
    public void setUpOnce() throws Exception{
        connectToServer();
    }
    
    public static void connectToServer() throws JAXRException {
        UDDINaming uddiNaming = new UDDINaming(uddiURL);
        endpointAddress = uddiNaming.lookup(serverName);

        if (endpointAddress == null) {
            System.out.println("The server \"" + serverName + "\" wasn't found");
            return;
        } else {
            System.out.println("The address \"" + endpointAddress + "\" was found");
        }

        System.out.println("Creating stub");
        SDId_Service service = new SDId_Service();
        server = service.getSDIdImplPort();

        System.out.println("Setting endpoint address");
        BindingProvider bindingProvider = (BindingProvider) server;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();

        requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
    }
}