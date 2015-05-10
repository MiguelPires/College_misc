package pt.tecnico.SDStore;

import java.util.*;

import pt.ulisboa.tecnico.sdis.store.ws.*;

public class Main {

	private static int id=0;
	private static Scanner scanner;

	//Various client tests - not working at 100% <------------------------------------------------ file just to test cliente-server
    public static void main(String[] args) {
    	// Check arguments
        if (args.length < 2) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s uddiURL name%n", StoreClient.class.getName());
            return;
        }
        scanner = new Scanner(System.in);
        String uddiURL = args[0];
        String name = args[1];
        StoreClient client = new StoreClient(uddiURL, name, id++);
        DocUserPair pair = new DocUserPair();
        pair.setUserId("alice");
        pair.setDocumentId("aaaaaaaaaa");
        System.out.println("Choose method");
        int command= -1;
        while(command!=0){
        	command= scanner.nextInt();
        if(command==3){
        try {
        	System.out.println("Choose string to store");
        	String file="9B7D2C34A366BF81";
        	String newfile = scanner.next();
        	
        	if(!newfile.equals("."))
        		file = newfile;
        	
			client.store(pair,file.getBytes());
		} catch (CapacityExceeded_Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (DocDoesNotExist_Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UserDoesNotExist_Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        System.out.println("stored");
        }
         
        if(command == 4){
        	System.out.println("Loading");
			try {
				System.out.println(client.load(pair));
			} catch (UserDoesNotExist_Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DocDoesNotExist_Exception e) {
			e.printStackTrace();
		}
    }
        if(command==2){
        	try {
				client.createDoc(pair);
			} catch (DocAlreadyExists_Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        if(command==1){
        	try {
        		System.out.println("Choose string list docs");
            	String user = scanner.next();
				client.listDocs(user);
			} catch (UserDoesNotExist_Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        }
        
        System.out.println("bye-bye - ERROR MAIN NEVER ENDS");
    }
}
