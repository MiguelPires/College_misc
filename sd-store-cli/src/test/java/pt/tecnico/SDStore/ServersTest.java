package pt.tecnico.SDStore;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.xml.registry.JAXRException;
import javax.xml.ws.handler.MessageContext;

import pt.tecnico.SDStore.handler.ClientHandler;
import pt.tecnico.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.store.ws.*;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Verifications;

import org.junit.*;

public class ServersTest {
	
    /* private static final String USER="alice";
     private static final String DOCID="aaaaaaaaaa";
     private static final byte[] CONTENT1 = "CONTENT1".getBytes();
     private static final byte[] CONTENT2 = "CONTENT2".getBytes();
     private static StoreClient client1;
	private static StoreClient client2;
     private static DocUserPair pair;
        
     @BeforeClass
     public static void setup() throws DocAlreadyExists_Exception, JAXRException{
        client1 = new StoreClient("http://localhost:8081", "SD-STORE", 1);
        client2 = new StoreClient("http://localhost:8081", "SD-STORE", 2);
        pair = new DocUserPair();
   	 	pair.setUserId(USER);
   	 	pair.setDocumentId(DOCID);
     }
     
     // tests if store and loading a doc works properly
     @Test
     public void sucess() throws DocAlreadyExists_Exception, CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception {
    	 client1.store(pair, CONTENT1);
    	 assertEquals(CONTENT1, client1.load(pair));
     }
     
     //tests if it's still working with a sleeping server
     @Test
     public void sucessOneFails() throws DocAlreadyExists_Exception, CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception {
    	 SecureSDStore server1 = new MockUp<SecureSDStore>()
    			 {
			 	@Mock
			 	public void storestore(DocUserPair docUserPair, byte[] contents) throws CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception, InterruptedException {
			 		Thread.sleep(10000);
			 	}
			 }.getMockInstance();
    			 
    	 client1.front.repManager.set(0, server1);
    	 client1.store(pair, CONTENT1);
    	 assertEquals(CONTENT1, client1.load(pair));
     }
     
     //Tests if sequence number works
     @Test
     public void testSeqNumber1() throws DocAlreadyExists_Exception, CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception, InterruptedException {
    	 
    	 client1.store(pair, CONTENT1);
    	 System.out.println(client1.load(pair));
    	 //assertEquals(CONTENT1, client1.load(pair));
    	client1.store(pair, CONTENT2);
    	System.out.println(client1.load(pair));
    	assertEquals(CONTENT2, client1.load(pair));
     }
     
     //Tests if sequence number works
     @Test
     public void testSeqNumber2() throws DocAlreadyExists_Exception, CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception {
    	 
    	 client1.store(pair, CONTENT1);
    	 assertEquals(CONTENT1, client1.load(pair));
    	 client2.store(pair, CONTENT2);
    	 assertEquals(CONTENT1, client2.load(pair));
     }
     
     //Tests if client ID works
     @Test
     public void testClientID1() throws DocAlreadyExists_Exception, CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception {
    	 client1.store(pair, CONTENT1);
    	 client2.store(pair, CONTENT2);
    	 assertEquals(CONTENT2, client1.load(pair));
     }
     
     //Tests if client ID works
     @Test
     public void testClientID2() throws DocAlreadyExists_Exception, CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception {
    	 client2.store(pair, CONTENT2);
    	 client1.store(pair, CONTENT1);
    	 assertEquals(CONTENT2, client1.load(pair));
     }*/

}
