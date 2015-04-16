package pt.tecnico.SDStore;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.sdis.store.ws.*;

import org.junit.*;

/**
 *  WSDL Contract Tests
 */
public class ContractTest {

     private static final String USER = "SD";
     private static final String DOC1USER = "Proj1";
     
     private static final String USERNOTEXIST = "ES";
     private static final String DOCNOTEXIST = "Lab";

     private static final byte[] CONTENT = new byte[1];
     private static final byte[] FULLCONTENT = new byte[10*1024];
     private static final DocUserPair pair = new DocUserPair();

     private static SDStoreImpl sdStore;

     @BeforeClass
     public static void oneTimeSetUp(){
    	 DocUserPair pair = new DocUserPair();
    	 pair.setUserId(USER);
       	 pair.setDocumentId(DOC1USER);
     }
     
     @Before
     public void setup() throws DocAlreadyExists_Exception{
      	pair.setUserId(USER);
      	pair.setDocumentId(DOC1USER);
      	sdStore= new SDStoreImpl();
      	sdStore.createDoc(pair);
     }
     
     
     //SUCCESS: create doc - user exist
     @Test
     public void createUserExist() 
             throws DocAlreadyExists_Exception, CapacityExceeded_Exception {
     	
     	pair.setDocumentId(DOCNOTEXIST);
     	
        sdStore.createDoc(pair);    
     }
     
    //SUCCESS: list user docs - user exist
    @Test
    public void listDocsSuccess() throws UserDoesNotExist_Exception, DocAlreadyExists_Exception {
    	List<String> expected = new ArrayList<String>();
    	expected.add(DOC1USER);
    	
    	List<String> res = sdStore.listDocs(USER);
        
        assertEquals(expected.get(0), res.get(0));
    }
 
    //FAIL: list user docs - user not exist
    @Test(expected = UserDoesNotExist_Exception.class)
    public void listUserNotExist() throws UserDoesNotExist_Exception {
        
        sdStore.listDocs(USERNOTEXIST);
    }

    //SUCCESS: create doc - user not exist
    @Test
    public void createUserNotExist() 
            throws DocAlreadyExists_Exception, CapacityExceeded_Exception {
        
    	pair.setUserId(USERNOTEXIST);
    	
        sdStore.createDoc(pair);
    }

    //FAIL: create doc - doc already exists
    @Test(expected = DocAlreadyExists_Exception.class)
    public void createDocAlreadyExists() 
            throws DocAlreadyExists_Exception, CapacityExceeded_Exception {
         
        sdStore.createDoc(pair); 
    } 

    //SUCCESS: replace doc content - doc exist, user exist, capacity not full
    @Test
    public void replaceDocSuccess()  
            throws CapacityExceeded_Exception, DocDoesNotExist_Exception, UserDoesNotExist_Exception, DocAlreadyExists_Exception {

        sdStore.store(pair, CONTENT);    
    }     

    //FAIL: replace doc content - user not exist 
    @Test(expected = UserDoesNotExist_Exception.class)
    public void replaceUserNotExist() 
            throws CapacityExceeded_Exception, DocDoesNotExist_Exception, 
            UserDoesNotExist_Exception {
        
    	pair.setUserId(USERNOTEXIST);
    	pair.setDocumentId(DOCNOTEXIST);
    	
        sdStore.store(pair, CONTENT);    
    }

    //FAIL: replace doc content - user exist, doc not exist
    @Test(expected = DocDoesNotExist_Exception.class)
    public void replaceDocNotExist() 
            throws CapacityExceeded_Exception, DocDoesNotExist_Exception, 
            UserDoesNotExist_Exception {
       
    	pair.setDocumentId(DOCNOTEXIST);
    	
       sdStore.store(pair, CONTENT);    
    }

    //FAIL: replace doc content - doc exist, user exist, capacity is full
    @Test(expected = CapacityExceeded_Exception.class)
    public void replaceCapacityFull() 
            throws CapacityExceeded_Exception, DocDoesNotExist_Exception, 
            UserDoesNotExist_Exception, DocAlreadyExists_Exception {
        
    	sdStore.store(pair, FULLCONTENT); 
    	
    	pair.setDocumentId(DOCNOTEXIST);
        sdStore.createDoc(pair); 
        sdStore.store(pair, CONTENT);
    }

    //SUCCESS: load doc - doc exist, user exist
    @Test
    public void loadSuccess() 
            throws DocDoesNotExist_Exception, UserDoesNotExist_Exception, CapacityExceeded_Exception, DocAlreadyExists_Exception {
    	
    	sdStore.store(pair, CONTENT); 
        byte[] content = sdStore.load(pair); 
        
        assertEquals(CONTENT,content);
    }     

    //FAIL: load doc - user not exist 
    @Test(expected = UserDoesNotExist_Exception.class)
    public void loadUserNotExist() 
            throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
    	
    	pair.setUserId(USERNOTEXIST);
    	
        sdStore.load(pair);    
    }

    //FAIL: load doc - user exist, doc not exist
    @Test(expected = DocDoesNotExist_Exception.class)
    public void loadDocNotExist() 
            throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
    	
    	pair.setDocumentId(DOCNOTEXIST);
    	
        sdStore.load(pair);
    }

    //SUCCESS: create doc, list user docs and load the new doc (has no content)
    @Test
    public void createListAndLoad() 
            throws DocDoesNotExist_Exception, UserDoesNotExist_Exception, 
            DocAlreadyExists_Exception, CapacityExceeded_Exception, 
            UserDoesNotExist_Exception {
        
    	pair.setDocumentId(DOCNOTEXIST);
    	
        sdStore.createDoc(pair);
        sdStore.listDocs(USER);
        byte[] content = sdStore.load(pair);

        assertNull(content);
    }

    //SUCCESS: create doc, replace the content to its maximum and then load the doc
    @Test
    public void createStoreAndLoad() 
            throws DocDoesNotExist_Exception, UserDoesNotExist_Exception, 
            DocAlreadyExists_Exception, CapacityExceeded_Exception,
            UserDoesNotExist_Exception {
    	
    	pair.setUserId(USERNOTEXIST);
    	pair.setDocumentId(DOCNOTEXIST);
    	
        sdStore.createDoc(pair);
        sdStore.store(pair, FULLCONTENT);
        byte[] content = sdStore.load(pair);
        
        assertEquals(FULLCONTENT,content);
    }
    
}
