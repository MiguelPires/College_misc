package pt.tecnico.SDStore;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.registry.JAXRException;
import javax.xml.ws.Endpoint;

import uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.store.ws.DocAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocUserPair;
import pt.ulisboa.tecnico.sdis.store.ws.SDStore;

import java.security.NoSuchAlgorithmException;

public class SDStoreMain {

    private static final int REPLICAS_NUMBER = 3;
    public static final boolean HANDLER_PRINT = false;

    public static void main(String[] args) {
        // Check arguments
        if (args.length < 3) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s uddiURL wsName wsURL%n", SDStoreMain.class.getName());
            return;
        }

        String uddiURL = args[0];
        String name = args[1];
        String url = args[2];

        ArrayList<Endpoint> endpoint = new ArrayList<Endpoint>(REPLICAS_NUMBER);
        UDDINaming uddiNaming = null;
        ArrayList <String> iWsURL = new ArrayList<String> (REPLICAS_NUMBER);

        try {
            uddiNaming = new UDDINaming(uddiURL);
            
            for (int i = 0; i < REPLICAS_NUMBER; i++) {
                String serverName = name + "-" + i;
                String[] split = url.split("localhost:");
                String[] oldPort = split[1].split("/");

                Integer port = Integer.parseInt(oldPort[0]) + 1;
                String newPort = port.toString();
                iWsURL.add(i, split[0] + "localhost:" + newPort + "/" + oldPort[1] + "/" + oldPort[2] + "-" + i);

                SecureSDStore secureStore = null;
                String key = "CYd/FbnCGtfTyr8uzJKeAw==";
                try {
                    secureStore = new SecureSDStore(new SDStoreImpl(serverName), key);
                } catch (NoSuchAlgorithmException e) {
                    System.out.printf("Caught exception when generating key", e);
                }

                endpoint.add(i, Endpoint.create(secureStore));

                // publish endpoint
                System.out.printf("Starting %s%n", iWsURL.get(i));
                endpoint.get(i).publish(iWsURL.get(i));

                // publish to UDDI
                if (uddiURL != null) {
                    System.out.printf("Publishing '%s' to UDDI at %s%n", serverName, uddiURL);
                    uddiNaming.rebind(serverName, iWsURL.get(i));
                }
            }

            // wait
            System.out.println("Awaiting connections");
            System.out.println("Press enter to shutdown");
            System.out.println("\n ******* SD-STORE *******");
            System.in.read();

        } catch (Exception e) {
            System.out.printf("Caught exception: %s%n", e);
            e.printStackTrace();

        } finally {
            try {
                for (int i = 0; i < REPLICAS_NUMBER; i++) {
                    if (endpoint.get(i) != null) {
                        // stop endpoint
                        endpoint.get(i).stop();
                        System.out.printf("Stopped %s%n", iWsURL.get(i));
                    }
                }
            } catch (Exception e) {
                System.out.printf("Caught exception when stopping: %s%n", e);
            }
            try {
                for (int i = 0; i < REPLICAS_NUMBER; i++) {
                    if (uddiNaming != null) {
                        uddiNaming.unbind(iWsURL.get(i));
                        System.out.printf("Deleted '%s' from UDDI%n", iWsURL.get(i));
                    }
                }
            } catch (Exception e) {
                System.out.printf("Caught exception when deleting: %s%n", e);
            }
        }

    }
}
