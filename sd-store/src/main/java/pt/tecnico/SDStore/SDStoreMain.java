package pt.tecnico.SDStore;

import javax.xml.registry.JAXRException;
import javax.xml.ws.Endpoint;

import pt.tecnico.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.store.ws.DocAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocUserPair;

public class SDStoreMain {
	
	private static SDStoreImpl Store;
	
	public static void setup() {
        try{
		DocUserPair pair = new DocUserPair();
		pair.setDocumentId("Doc1");
		pair.setUserId("alice");
		Store.createDoc(pair);
		
		pair.setUserId("bruno");
		Store.createDoc(pair);
		
		pair.setUserId("carla");
		Store.createDoc(pair);
		
		pair.setUserId("duarte");
		Store.createDoc(pair);
		
		pair.setUserId("eduardo");
		Store.createDoc(pair);
        } catch (DocAlreadyExists_Exception e1) {
            ;
        }
	}
	
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
        
        Endpoint endpoint = null;
        UDDINaming uddiNaming = null;
        Store=new SDStoreImpl();
        SecureSDStore secureStore = new SecureSDStore(Store);
        
		setup();
        
        try {
            endpoint = Endpoint.create(secureStore);

            // publish endpoint
            System.out.printf("Starting %s%n", url);
            endpoint.publish(url);

            // publish to UDDI
            System.out.printf("Publishing '%s' to UDDI at %s%n", name, uddiURL);
            uddiNaming = new UDDINaming(uddiURL);
            uddiNaming.rebind(name, url);
        
                        
            // wait
            System.out.println("Awaiting connections");
            System.out.println("Press enter to shutdown");
            System.in.read();

        } catch(Exception e) {
            System.out.printf("Caught exception: %s%n", e);
            e.printStackTrace();

        } finally {
            try {
                if (endpoint != null) {
                    // stop endpoint
                    endpoint.stop();
                    System.out.printf("Stopped %s%n", url);
                }
            } catch(Exception e) {
                System.out.printf("Caught exception when stopping: %s%n", e);
            }
            try {
                if (uddiNaming != null) {
                    // delete from UDDI
                    uddiNaming.unbind(name);
                    System.out.printf("Deleted '%s' from UDDI%n", name);
                }
            } catch(Exception e) {
                System.out.printf("Caught exception when deleting: %s%n", e);
            }
        }

    }

}
