package pt.tecnico.SDStore;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import static org.junit.Assert.*;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;

import pt.ulisboa.tecnico.sdis.store.ws.*;

import org.junit.*;

public class ImplementationTest {
	
	 private static final String USER_1 = "alice";
	 private static final String USER_2 = "bruno";
	 private static final String USER_3 = "carla";
	 private static final String USER_4 = "duarte";
	 private static final String USER_5 = "eduardo";
     private static final String DOC1USER = "Proj1";
     
     private static final String USERNOTEXIST = "ES";
     private static final String DOCNOTEXIST = "Lab";

     private static final byte[] CONTENT = new byte[1];
     private static final byte[] FULLCONTENT = new byte[10*1024];
     private static final byte[] OVERSIZEDCONTENT = new byte[10*1025];
     
     private static DocUserPair pair;

     private static SDStoreImpl sdStore;
     
     private static userDirectory userDir;
     
     private static document doc;
     
     
     @Before
     public void setup() throws DocAlreadyExists_Exception{
    	 pair = new DocUserPair();
    	pair.setUserId(USER_1);
        pair.setDocumentId(DOC1USER);
        sdStore= new SDStoreImpl();
        userDir = new userDirectory(USER_1);
     }
     
     @After
     public void tearDown(){	
    	 sdStore=null;
     }
     
     @Test
   //SUCCESS: addUser to DIr
     public void successAddUsertoDir()
    	 throws UserDoesNotExist_Exception, CapacityExceeded_Exception{
    	 
    		 pair.setUserId(USER_2);
     }
     
     @Test
   //SUCCESS: updateDoc in dir
     public void successUpdateDoc()
     	throws CapacityExceeded_Exception, DocDoesNotExist_Exception, DocAlreadyExists_Exception{
    	
    	 userDir.updateDoc(doc, CONTENT);
     }
     
     @Test
   //SUCCESS: user not exist in dir
     public void userNotExistInDir()
     	throws UserDoesNotExist_Exception{
    	 
    	 assertEquals(USER_4,userDir.getUser());
     }
     

    	 
    
     

}
