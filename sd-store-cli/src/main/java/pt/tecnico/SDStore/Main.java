package pt.tecnico.SDStore;

import java.util.*;

import pt.ulisboa.tecnico.sdis.store.ws.*;

public class Main {

	private static int id=0;
	private static Scanner scanner;

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
        
        System.out.println("############## WELCOME ##############");
        System.out.println("1 - List Documents of user");
        System.out.println("2 - Create new document");
        System.out.println("3 - Store document content");
        System.out.println("4 - Load document  content");
        System.out.println("5 - Change user or document ID");
        System.out.println("0 - Exit");

        int command= -1;
        while(command!=0){
        	System.out.println("# Choose command");
        	command= scanner.nextInt();
        	
        	if(command==1){
        		try {
               		System.out.println("# Choose username to list docs");
                   	String user = scanner.next();
       				List<String> result = client.listDocs(user);
       				for(String doc : result)
       					System.out.println(doc);
       			} catch (UserDoesNotExist_Exception e) {
       		        System.out.println("# User does not exists");
       			}
            }
        	
        	if(command==2){
            	try {
            		System.out.println("# Choose doc ID");
            		pair.setDocumentId(scanner.next());
    				client.createDoc(pair);
    				System.out.println("# Created");
    			} catch (DocAlreadyExists_Exception e) {
    				System.out.println("# Document already exists");
    			}
            }
        	
        	if(command==3){
        		try {
        			System.out.println("# Insert content to store"); 
        			scanner.nextLine();
        			String file = scanner.nextLine();
        			client.store(pair, file.getBytes());
        			System.out.println("# File stored");
        		} catch (CapacityExceeded_Exception e1) {
        			System.out.println("# Capacity exceeded - should never happen");
        		} catch (DocDoesNotExist_Exception e1) {
        			System.out.println("# Document does not exist");
        		} catch (UserDoesNotExist_Exception e1) {
        			System.out.println("# User does not exist");
        		}
        	}
         
        	if(command == 4){
        		try {
        			System.out.println(new String(client.load(pair)));
        		} catch (UserDoesNotExist_Exception e) {
            		System.out.println("# User does not exist");
        		} catch (DocDoesNotExist_Exception e) {
            		System.out.println("# Document does not exist");
        		}
        	}
        	
        	if(command==5){
        		System.out.println("# Choose username");
        		pair.setUserId(scanner.next());
        		System.out.println("# Choose doc ID");
        		pair.setDocumentId(scanner.next());
        	}
        }
        scanner.close();
        System.out.println("# bye-bye");
    }
}
