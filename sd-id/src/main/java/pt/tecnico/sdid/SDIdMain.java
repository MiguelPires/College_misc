package pt.tecnico.sdid;

import javax.xml.ws.Endpoint;

import pt.tecnico.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.id.ws.CreateUser;
import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.SDId;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists;

public class SDIdMain {

    private static SDIdImpl server;

    public static void populate() throws EmailAlreadyExists_Exception,
            InvalidEmail_Exception,
            UserAlreadyExists {
        // Alice 
        User alice = new User("alice", "alice@tecnico.pt", "Aaa1");
        server.getUsers().add(alice);

        // Bruno
        User bruno = new User("bruno", "bruno@tecnico.pt", "Bbb2");
        server.getUsers().add(bruno);

        // Carla
        User carla = new User("carla", "carla@tecnico.pt", "Ccc3");
        server.getUsers().add(carla);

        // Duarte
        User duarte = new User("duarte", "duarte@tecnico.pt", "Ddd4");
        server.getUsers().add(duarte);


        // Eduardo 
        User eduardo = new User("eduardo", "eduardo@tecnico.pt", "Eee5");
        server.getUsers().add(eduardo);
    }

    public static void main(String[] args) throws EmailAlreadyExists_Exception,
            InvalidEmail_Exception,
            UserAlreadyExists {
        // Check arguments
        if (args.length < 1) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s url%n", SDIdMain.class.getName());
            return;
        }

        String uddiUrl = args[0];
        String name = args[1];
        String url = args[2];

        Endpoint endpoint = null;
        try {

            server = new SDIdImpl();
            populate();
            endpoint = Endpoint.create(server);

            // publish endpoint
            System.out.printf("Starting %s%n", url);
            endpoint.publish(url);

            // publish to UDDI
            System.out.printf("Publishing '%s' to UDDI at %s%n", name, uddiUrl);
            UDDINaming uddiNaming = new UDDINaming(uddiUrl);
            uddiNaming.rebind(name, url);

            // wait
            System.out.println("Awaiting connections");
            System.out.println("Press enter to shutdown");
            System.in.read();

        } catch (Exception e) {
            System.out.printf("Caught exception: %s%n", e);
            e.printStackTrace();

        } finally {
            try {
                if (endpoint != null) {
                    // stop endpoint
                    endpoint.stop();
                    System.out.printf("Stopped %s%n", url);
                }
            } catch (Exception e) {
                System.out.printf("Caught exception when stopping: %s%n", e);
            }
        }

    }
}
