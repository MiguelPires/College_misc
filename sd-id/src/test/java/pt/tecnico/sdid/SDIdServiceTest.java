package pt.tecnico.sdid;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;

import org.junit.After;
import org.junit.Before;

import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.SDId;
import pt.ulisboa.tecnico.sdis.id.ws.SDId_Service;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists_Exception;
import uddi.UDDINaming;

public class SDIdServiceTest {

    // for setup
    public static String serverName = System.getProperty("test.name");
    public static String uddiURL = System.getProperty("uddi.url");
    public static String serverURL = System.getProperty("test.url");

    // client side reference
    public static SDId cServer;

    // server side reference
    public static SDIdImpl sServer;
    public static String endpointAddress;

    static Endpoint endpoint;
    static UDDINaming uddiNaming;

    @Before
    public void setUp() throws JAXRException, EmailAlreadyExists_Exception, InvalidEmail_Exception,
                       UserAlreadyExists_Exception, InvalidUser_Exception {
        setUpServer();
        connectToServer();
    }

    @After
    public void tearDown() throws JAXRException {
        if (endpoint != null)
            endpoint.stop();
        cServer = null;
        sServer = null;
        endpointAddress = null;

        if (uddiNaming != null)
            uddiNaming.unbind(serverName);
    }

    public static void setUpServer() throws JAXRException, EmailAlreadyExists_Exception,
                                    InvalidEmail_Exception, UserAlreadyExists_Exception,
                                    InvalidUser_Exception {

        SDIdImpl sServer = new SDIdImpl();
        SDIdMain.populate(sServer);
        endpoint = Endpoint.create(sServer);

        // publish endpoint
        endpoint.publish(serverURL);

        // publish to UDDI
        uddiNaming = new UDDINaming(uddiURL);
        uddiNaming.rebind(serverName, serverURL);
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
        cServer = service.getSDIdImplPort();

        System.out.println("Setting endpoint address");
        BindingProvider bindingProvider = (BindingProvider) cServer;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();

        requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
    }
}
