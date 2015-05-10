package pt.tecnico.SDStore;

import java.util.*;

import javax.xml.registry.JAXRException;
import javax.xml.ws.*;

import com.sun.xml.ws.api.EndpointAddress;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import pt.tecnico.uddi.UDDINaming;

import pt.ulisboa.tecnico.sdis.store.ws.*;
import pt.tecnico.SDStore.handler.RelayClientHandler;

public class StoreClient {


    public static void main(String[] args) {
    	// Check arguments
        if (args.length < 2) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s uddiURL name%n", StoreClient.class.getName());
            return;
        }

        String uddiURL = args[0];
        String name = args[1];
        System.out.printf("Contacting UDDI at %s%n", uddiURL);
        UDDINaming uddiNaming=null;
        
		try {
			uddiNaming = new UDDINaming(uddiURL);
		} catch (JAXRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        System.out.printf("Looking for '%s'%n", name);
        String endpointAddress=null;
		try {
			endpointAddress = uddiNaming.lookup(name+0);
		} catch (JAXRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        if (endpointAddress == null) {
            System.out.println("Not found!");
            return;
        } else {
            System.out.printf("Found %s%n", endpointAddress);
        }

        System.out.println("Creating stub ...");
        SDStore_Service service = new SDStore_Service();
        SDStore port = service.getSDStoreImplPort();

        BindingProvider bindingProvider = (BindingProvider) port;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
         
        requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);

        StoreClient client = new StoreClient(port);
        client.startClient();
    }

    private SDStore port;
    private Scanner scanner;

    public StoreClient(SDStore port){
        this.port = port;
        scanner = new Scanner(System.in);
    }

    public void startClient(){
        int command=-1;

        while(command!=0){
            System.out.println("Escolha um comando:");
            command = scanner.nextInt();
            if(command==1){
                ;
            }

            if(command==2){
                DocUserPair pair = new DocUserPair();
                pair.setDocumentId("doc");
                pair.setUserId("user");

                BindingProvider bindingProvider = (BindingProvider) port;
         Map<String, Object> requestContext = bindingProvider.getRequestContext();
         String initialValue = "009;444";
         requestContext.put(RelayClientHandler.REQUEST_PROPERTY, initialValue);
                
                try{
                port.createDoc(pair);
            }catch (DocAlreadyExists_Exception e){
                ;
            }
            
                  Map<String, Object> responseContext = bindingProvider.getResponseContext();
                 String finalValue = (String) responseContext.get(RelayClientHandler.RESPONSE_PROPERTY);
            System.out.printf("%s got token '%s' from response context%n", "dwedwe", finalValue);
                    }

            if(command==3){
                String s = "9B7D2C34A366BF81";
                DocUserPair pair = new DocUserPair();
                pair.setDocumentId("doc");
                pair.setUserId("user");
                 try{
                port.createDoc(pair);
            }catch (DocAlreadyExists_Exception e){
                ;
            }

            //BindingProvider bindingProvider = (BindingProvider) port;
              //  Map<String, Object> requestContext = bindingProvider.getRequestContext();
               
                try{
                port.store(pair, s.getBytes());
            } catch(Exception e){
                System.out.println("ERRORRRR");
            }

                System.out.println(s.getBytes());
                byte[] ss=null;
                try{
                ss = port.load(pair);
            } catch(Exception e){
                System.out.println("ERRORRRR");
            }
                System.out.println(new String(ss));

                
            }

            if(command==4){
                ;
            }

        }
    }

}
