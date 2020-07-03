package pt.tecnico.SDStore;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.registry.JAXRException;

import pt.tecnico.SDStore.handler.ClientHandler;
import pt.ulisboa.tecnico.sdis.store.ws.*;

import org.junit.*;

public class ServersTest {
	
     private static final String USER="alice";
     private static final String DOCID="aaaaaaaaaa";
     private static final byte[] CONTENT1 = "CONTENT1".getBytes();
     private static final byte[] CONTENT2 = "CONTENT2".getBytes();
     private static StoreClient client1;
     private static StoreClient client2;
     private static StoreClient client3;
        
     @BeforeClass
     public static void setup() throws DocAlreadyExists_Exception, JAXRException{
        client1 = new StoreClient("http://localhost:8081", "SD-STORE", 1);
        client2 = new StoreClient("http://localhost:8081", "SD-STORE", 2);
        client3 = new StoreClient("http://localhost:8081", "SD-STORE", 1);
     }
     
     // tests if store and loading a doc works properly
     @Test
     public void success() throws DocAlreadyExists_Exception, CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception {
    	 client1.store(USER, DOCID, CONTENT1);
    	 byte[] content = client1.load(USER, DOCID);
    	 assertTrue(new String(CONTENT1).equals(new String(content)));
     }
     
     //Tests if sequence number works
     @Test
     public void testSeqNumber1() throws DocAlreadyExists_Exception, CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception, InterruptedException {
    	 
    	 client1.store(USER, DOCID, CONTENT1);
    	 byte[] content = client1.load(USER, DOCID);
    	 assertTrue(new String(CONTENT1).equals(new String(content)));
    	 client1.store(USER, DOCID, CONTENT2);
    	 content = client1.load(USER, DOCID);
    	 assertTrue(new String(CONTENT2).equals(new String(content)));
     }
     
     //Tests if sequence number works
     @Test
     public void testSeqNumber2() throws DocAlreadyExists_Exception, CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception {
    	 
    	 client1.store(USER, DOCID, CONTENT1);
    	 byte[] content = client1.load(USER, DOCID);
    	 assertTrue(new String(CONTENT1).equals(new String(content)));
    	 
    	 client3.store(USER, DOCID, CONTENT2);
    	 content = client3.load(USER, DOCID);
    	 assertTrue(new String(CONTENT2).equals(new String(content)));
     }
     
     //Tests if client ID works
     @Test
     public void testClientID1() throws DocAlreadyExists_Exception, CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception {
    	DocUserPair pair = new DocUserPair();
    	pair.setUserId(USER);
    	pair.setDocumentId(DOCID);
    	String[] tag1 = { "123" , "1" };
    	String[] tag2 = { "123" , "2" };
    	client1.front.store(pair, CONTENT1, tag1);
    	client2.front.store(pair, CONTENT2, tag2);
    	byte[] content = client1.load(USER, DOCID);
    	assertTrue(new String(CONTENT2).equals(new String(content)));
     }
     
     //Tests if client ID works
     @Test
     public void testClientID2() throws DocAlreadyExists_Exception, CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception {
    	DocUserPair pair = new DocUserPair();
     	pair.setUserId(USER);
     	pair.setDocumentId(DOCID);
     	String[] tag1 = { "123" , "1" };
     	String[] tag2 = { "123" , "2" };
     	
     	client2.front.store(pair, CONTENT2, tag2);
     	client1.front.store(pair, CONTENT1, tag1);
    	byte[] content = client1.load(USER, DOCID);
    	assertTrue(new String(CONTENT2).equals(new String(content)));
     }
     
     @Test
     public void oneFails() throws JAXRException, UserDoesNotExist_Exception, CapacityExceeded_Exception, DocDoesNotExist_Exception{
    	 
    	 System.out.println("############# Shutdown 1 server please ################");
    	 try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 
    	 System.out.println("############# Resuming ################");
    	 client1.store(USER, DOCID, CONTENT1);
    	 byte[] content = client1.load(USER, DOCID);
    	 assertTrue(new String(CONTENT1).equals(new String(content)));
     }
}
