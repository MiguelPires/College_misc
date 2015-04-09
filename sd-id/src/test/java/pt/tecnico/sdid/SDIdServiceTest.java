package pt.tecnico.sdid;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import org.junit.Before;

import pt.tecnico.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.id.ws.SDId;
import pt.ulisboa.tecnico.sdis.id.ws.SDId_Service;

public class SDIdServiceTest {

    // alterar para argumentos do pom
    String serverName = "SDId";
    String uddiUrl = "http://localhost:8081";
    SDId server;

    @Before
    public void setUp() throws Exception {
        server = getServer();
    }

    public SDId getServer() throws JAXRException {
        UDDINaming uddiNaming = new UDDINaming(uddiUrl);
        String endpointAddress = uddiNaming.lookup(serverName);

        if (endpointAddress == null) {
            System.out.println("The server \"" + serverName + "\" wasn't found");
            return null;
        } else {
            System.out.println("The address \"" + endpointAddress + "\" was found");
        }

        System.out.println("Creating stub");
        SDId_Service service = new SDId_Service();
        SDId port = service.getSDIdImplPort();

        System.out.println("Setting endpoint address");
        BindingProvider bindingProvider = (BindingProvider) port;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();

        requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);

        return port;
    }
}
