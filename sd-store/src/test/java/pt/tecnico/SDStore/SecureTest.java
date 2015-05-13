package pt.tecnico.SDStore;

import org.junit.*;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;
import java.util.*;

import pt.ulisboa.tecnico.sdis.store.ws.*;

/**
 *  Security Tests
 */

public class SecureTest {

     private static final String USER = "SD";
     private static final String DOC1USER = "Proj1";
     
     private static final String NOTEXISTALGORITHM = "SDist";
     private static final String EXISTALGORITHM = "AES";

     private static final byte[] CONTENT = "HELLO".getBytes();
     
     private static DocUserPair pair;

     private static SDStoreImpl Store;
     private static SecureSDStore secureStore;
        
     @Before
     public void setup() throws DocAlreadyExists_Exception {
    	pair = new DocUserPair();
    	pair.setUserId(USER);
        pair.setDocumentId(DOC1USER);
        
        Store = new SDStoreImpl("SD");
        
        secureStore = null;
              
        try {          
            secureStore = new SecureSDStore(Store, "KEY");
            } catch(NoSuchAlgorithmException e) {
            System.out.printf("Caught exception when generating key", e);
        }      
     }
     
     @After
     public void tearDown() {	
    	 secureStore=null;
     }
     
    
    //SUCCESS: Generate key with existent algorithm
    @Test
    public void serverGenerateKey() throws Exception {
     
        assertNotNull(secureStore.generateKey(EXISTALGORITHM));
    }

    //FAIL: Generate key with nonexistent algorithm
    @Test(expected = NoSuchAlgorithmException.class)
    public void serverFailGenerateKey() throws Exception {
     
        secureStore.generateKey(NOTEXISTALGORITHM);
    }

    //SUCCESS: Test if server encrypts properly a given content
    @Test
    public void serverCipher() throws Exception {

    	byte[] cipheredContent = secureStore.cipher(pair.getDocumentId(), CONTENT);
    	assertNotNull(cipheredContent);
    }

    //SUCCESS: Test if server encrypts/decrypts properly a given content
    @Test
    public void MACDecipher() throws Exception {

        byte[] cipheredContent = secureStore.cipher(pair.getDocumentId(), CONTENT);
        byte[] decipheredContent = secureStore.decipher(pair.getDocumentId(), cipheredContent);
        assertTrue(new String(CONTENT).equals(new String(decipheredContent))); 
    } 
    
    @Test
    public void simpleDecipher() throws Exception {

        byte[] cipheredContent = secureStore.cipherMessage("Encrypt", CONTENT);
        byte[] decipheredContent = secureStore.cipherMessage("Decrypt", cipheredContent);
        assertTrue(new String(CONTENT).equals(new String(decipheredContent))); 
    } 

}