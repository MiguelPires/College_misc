package pt.tecnico.sdid;

import java.io.IOException;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;

import pt.tecnico.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.SDId;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists_Exception;

public class SDIdMain {

    private static SDIdImpl server;

    public static void populate() throws EmailAlreadyExists_Exception,
            InvalidEmail_Exception,
            UserAlreadyExists_Exception, InvalidUser_Exception {

        server.addUser("alice", "alice@tecnico.pt", "Aaa1");
        server.addUser("bruno", "bruno@tecnico.pt", "Bbb2");
        server.addUser("carla", "carla@tecnico.pt", "Ccc3");
        server.addUser("duarte", "duarte@tecnico.pt", "Ddd4");
        server.addUser("eduardo", "eduardo@tecnico.pt", "Eee5");
    }
    

    public static void main(String[] args) throws EmailAlreadyExists_Exception,
            InvalidEmail_Exception,
            UserAlreadyExists_Exception, IOException, InvalidUser_Exception {
        
        Endpoint endpoint = null;
        
        try {
            // Check arguments
            if (args.length < 1) {
                System.err.println("Argument(s) missing!");
                System.err.printf("Usage: java %s url%n", SDIdMain.class.getName());
                return;
            }

            String uddiUrl = args[0];
            String name = args[1];
            String url = args[2];

            endpoint = null;

            server = new SDIdImpl();
            populate();
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

                    // runtime error 
                    BindingProvider bindingProvider = (BindingProvider) server;
                    EndpointReference ref = bindingProvider.getEndpointReference();
                    System.out.println("Alternative url: "+ref.toString());
                    uddiNaming.rebind(name, ref.toString());
                } catch (JAXRException e1) {
                    // keep server running without being published
                }
            } 

            // wait
            System.out.println("Awaiting connections");
            System.out.println("Press enter to shutdown");
            System.in.read();
        } finally {
            if (endpoint != null) 
                endpoint.stop();
        }
    }
}
