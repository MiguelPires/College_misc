package pt.tecnico.sdid;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;

import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.SDId;
import pt.ulisboa.tecnico.sdis.id.ws.SDId_Service;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists_Exception;
import uddi.UDDINaming;

public class SDIdMain {

    private static SDIdImpl server;
    private static Endpoint endpoint;

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public static void populate(SDIdImpl server) throws EmailAlreadyExists_Exception,
                                                InvalidEmail_Exception,
                                                UserAlreadyExists_Exception, InvalidUser_Exception, NoSuchAlgorithmException, InvalidKeySpecException {
        server.addUser("alice", "alice@tecnico.pt", "Aaa1");
        server.addUser("bruno", "bruno@tecnico.pt", "Bbb2");
        server.addUser("carla", "carla@tecnico.pt", "Ccc3");
        server.addUser("duarte", "duarte@tecnico.pt", "Ddd4");
        server.addUser("eduardo", "eduardo@tecnico.pt", "Eee5");
    }

    public static void main(String[] args) throws EmailAlreadyExists_Exception,
                                          InvalidEmail_Exception, UserAlreadyExists_Exception,
                                          IOException, InvalidUser_Exception {

        try {
            setUp(args);

            // wait
            System.out.println("Awaiting connections");
            System.out.println("\n ******* SD-ID *******");
            System.in.read();
        } catch (Exception e) {
        }

        if (endpoint != null)
            endpoint.stop();
    }

    public static void setUp(String[] args) throws EmailAlreadyExists_Exception,
                                           InvalidEmail_Exception, UserAlreadyExists_Exception,
                                           IOException, InvalidUser_Exception, NoSuchAlgorithmException, InvalidKeySpecException {

        // Check arguments
        if (args.length < 1) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s url%n", SDIdMain.class.getName());
            return;
        }

        String uddiUrl = args[0];
        String name = args[1];
        String url = args[2];

        // client and server keys
        server = new SDIdImpl(args[3], args[4]);
        populate(server);
        endpoint = Endpoint.create(server);

        // publish endpoint
        System.out.printf("Starting %s%n", url);
        endpoint.publish(url);

        // publish to UDDI
        System.out.printf("Publishing '%s' to UDDI at %s%n", name, uddiUrl);

        UDDINaming uddiNaming = null;
        try {
            uddiNaming = new UDDINaming(uddiUrl);
            uddiNaming.rebind(name, url);
        } catch (JAXRException e) {
            try {

                SDId_Service service = new SDId_Service();
                SDId newServer = service.getSDIdImplPort();
                
                BindingProvider bindingProvider = (BindingProvider) newServer;
                Map<String, Object> requestContext = bindingProvider.getRequestContext();
                Object v = requestContext.getOrDefault(ENDPOINT_ADDRESS_PROPERTY, "");

                System.out.println("Alternative url: " + v.toString());

                uddiNaming.rebind(name, v.toString());

            } catch (JAXRException e1) {
                // keep server running without being published
            }
        }
    }
}
