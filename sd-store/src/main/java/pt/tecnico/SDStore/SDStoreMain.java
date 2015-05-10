package pt.tecnico.SDStore;

import java.util.Collection;

import javax.xml.registry.JAXRException;
import javax.xml.ws.Endpoint;

import pt.tecnico.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.store.ws.DocAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.DocUserPair;
import java.security.NoSuchAlgorithmException;
import java.security.Key;
import javax.crypto.KeyGenerator;

public class SDStoreMain {
	
	private static SDStoreImpl Store;
	
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
        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        keyGen.init(128);
        Key key = keyGen.generateKey();
        SecureSDStore secureStore = new SecureSDStore(Store, key);
        
        try {
        	uddiNaming = new UDDINaming(uddiURL);
        	int id=0;
        	String auxName = name;
        	name=name+id;
        	while(uddiNaming.lookup(name)!=null){
        		id++;
        		name = auxName + id;
        	}
        		
        	if(id!=0){
        		// creates next url (changing the port number) for the next server to be created
        		String[] split = url.split("/store-ws");
        		char[] newString = split[0].toCharArray();
        		newString[newString.length-1] = new Integer(2+id).toString().toCharArray()[0];
        		split[0] = String.valueOf(newString);
        		url = split[0] + "/store-ws" + split[1];
        	}
        	
            endpoint = Endpoint.create(secureStore);

            // publish endpoint
            System.out.printf("Starting %s%n", url);
            endpoint.publish(url);

            // publish to UDDI
            System.out.printf("Publishing '%s' to UDDI at %s%n", name, uddiURL);
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
