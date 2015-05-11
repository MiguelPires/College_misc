package pt.tecnico.SDStore;

import org.junit.*;
import static org.junit.Assert.*;

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

     private static final byte[] CONTENT = new byte[1];
     private static final byte[] HACKEDCONTENT_SAMELENGTH = new byte[1];
     private static final byte[] HACKEDCONTENT_DIFLENGTH = new byte[5];
     
     private static DocUserPair pair;

     private static SDStoreImpl Store;

        
     @Before
     public void setup() throws DocAlreadyExists_Exception {
    	pair = new DocUserPair();
    	pair.setUserId(USER);
        pair.setDocumentId(DOC1USER);
        
        Arrays.fill(CONTENT, (byte) 1);
        Arrays.fill(HACKEDCONTENT_SAMELENGTH, (byte) 2);
        Arrays.fill(HACKEDCONTENT_DIFLENGTH, (byte) 0);
        
        Store = new SDStoreImpl();
        
        SecureSDStore secureStore = null;
              
        try {          
            secureStore = new SecureSDStore(Store);
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
     
        secureStore.generateKey(EXISTALGO);
    }

    //FAIL: Generate key with nonexistent algorithm
    @Test(expected = NoSuchAlgorithmException)
    public void serverFailGenerateKey() throws Exception {
     
        secureStore.generateKey(NOEXISTALGO);
    }

    //SUCCESS: Test if server encripts properly a given content
    @Test
    public void serverCipher() throws Exception {

    	byte[] cipheredContent = secureStore.cipher(pair.getDocumentId(), CONTENT);
    }

    //SUCCESS: Test if server decripts properly a given content
    @Test
    public void serverDecipher() throws Exception {

        byte[] cipheredContent = secureStore.cipher(pair.getDocumentId(), CONTENT);
        byte[] decipheredContent = secureStore.decipher(pair.getDocumentId(), cipheredContent);
        assertEquals(CONTENT, decipheredContent); 
    } 

    //SUCCESS: Store content and load it, checking if it's the same after encriptation/decriptation
    @Test
    public void storeAndLoad() throws Exception {
     
        secureStore.store(pair, CONTENT);
        byte[] doc = secureStore.load(pair);
        assertEquals(doc, CONTENT);
    } 

}