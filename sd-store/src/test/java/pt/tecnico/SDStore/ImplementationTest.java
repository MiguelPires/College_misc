package pt.tecnico.SDStore;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.xml.registry.JAXRException;
import javax.xml.ws.handler.MessageContext;

import pt.tecnico.SDStore.handler.StoreServerHandler;
import pt.tecnico.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.store.ws.*;
import mockit.Mock;
import mockit.MockUp;

import org.junit.*;

public class ImplementationTest {
	
     private static final String USER="alice";
     private static final String DOCID="Docx";
     private static final byte[] CONTENT = new byte[1];
     private static final byte[] FULLCONTENT = new byte[10*1024];
     private static final byte[] OVERSIZEDCONTENT = new byte[10*1025];
     private static userDirectory directory;
     private static document doc;
        
     @Before
     public void setup() throws DocAlreadyExists_Exception{
        directory = new userDirectory(USER);
        doc = new document(DOCID);
     }
     

     // Tests if doc exists after addition (tests docExists() function)
     @Test
     public void addDocument() throws DocAlreadyExists_Exception {
      directory.addDoc(DOCID);
      
      assertEquals(true, directory.docExists(DOCID));
     }
    
     //Tests if directory has space after one document store (1 byte)  (tests isFull() function);
    /* @Test
     public void addContent() throws DocAlreadyExists_Exception, CapacityExceeded_Exception, DocDoesNotExist_Exception {
    	 String[] tag = {"1","1"};
      directory.addDoc(DOCID);
      directory.storeContent(DOCID, CONTENT, tag);
      
      assertEquals(false, directory.isFull());
     }
   
   //Tests if content and directory size is updated properly;
     @Test
     public void updateDocTest() throws DocAlreadyExists_Exception, CapacityExceeded_Exception, DocDoesNotExist_Exception {
         directory.updateDoc(doc, CONTENT);
        
         assertEquals(CONTENT, doc.getContent());
         assertEquals((int)directory.getCapacity().getCurrentSize(), CONTENT.length);
         
         directory.updateDoc(doc, FULLCONTENT);
         
         assertEquals(FULLCONTENT, doc.getContent());
         assertEquals((int)directory.getCapacity().getCurrentSize(), FULLCONTENT.length);
         
         try{
         directory.updateDoc(doc, new byte[10*1025]);
         } catch(CapacityExceeded_Exception e){
             ;
         }
         
         assertEquals((int)directory.getCapacity().getCurrentSize(), FULLCONTENT.length);
     }*/
    
     //Tests if tags compare properly
     @Test
     public void compareTags() {
      tag tag1 = new tag();
      tag tag2 = new tag(1,1);
      tag tag3 = new tag(0,1);
      
      assertTrue(tag2.isGreater(tag1));
      assertTrue(tag3.isGreater(tag1));
      assertFalse(tag1.isGreater(tag3));
     }
     
     //Tests if storing docs only store if tag is greater
     @Test
     public void storeTestTags() throws DocDoesNotExist_Exception, CapacityExceeded_Exception, DocAlreadyExists_Exception {
        
		directory.addDoc(DOCID);
         byte[] content = "CONTENT".getBytes();
         byte[] newContent = "NEWCONTENT".getBytes();
         String[] tag = {"1", "1"};
         
		 directory.storeContent(DOCID, content, tag);
		
		assertEquals(directory.loadDoc(DOCID).getContent(), content);
         
 		directory.storeContent(DOCID, newContent, tag);
         
 		assertEquals(directory.loadDoc(DOCID).getContent(), content);
        }

}
