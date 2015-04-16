package pt.tecnico.SDStore;

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

     private static final String CONTENT = "11110";
     private static final byte[] CONTENTB = CONTENT.getBytes();
     private static final byte[] CONTENTFULL = CONTENT; //Alterar para o max que consegue

     private static SDStoreImpl sdStore;

     sdStore = new SDStoreImpl();

     @Override
     public void populate() {
       
        userDirectory USER = new userDirectory();
        sdStore.getFolders().add(USER);
        sdStore.getUserRepository(USER).addDoc(DOC1USER);

     }

    //SUCCESS: list user docs - user exist
    @Test
    public void listDocsSuccess() throws UserDoesNotExist_Exception {
        
        sdStore.listDocs(USER);
    }
 
    //FAIL: list user docs - user not exist
    @Test(expected = UserDoesNotExist_Exception.class)
    public void listUserNotExist() throws UserDoesNotExist_Exception {
        
        sdStore.listDocs(USERNOTEXIST);
    }

    //SUCCESS: create doc - user exist
    @Test
    public void createUserExist() 
            throws DocAlreadyExists_Exception, CapacityExceeded_Exception {
        
        sdStore.createDoc(DOCNOTEXIST, USER);    
    }

    //SUCCESS: create doc - user not exist
    @Test
    public void createUserNotExist() 
            throws DocAlreadyExists_Exception, CapacityExceeded_Exception {
        
        sdStore.createDoc(DOCNOTEXIST, USERNOTEXIST);
    }

    //FAIL: create doc - doc already exists
    @Test(expected = DocAlreadyExists_Exception.class)
    public void createDocAlreadyExists() 
            throws DocAlreadyExists_Exception, CapacityExceeded_Exception {
        
        sdStore.createDoc(DOC1USER, USER);    
    } 

    //FAIL: create doc - user repository storage capacity is full
    @Test(expected = CapacityExceeded_Exception.class)
    public void createCapacityFull() 
            throws DocAlreadyExists_Exception, CapacityExceeded_Exception {
        
        while (1) {
            int i = 0;
            sdStore.createDoc(DOCNOTEXIST+"i", USER);
            i ++;
        }
    }   

    //SUCCESS: replace doc content - doc exist, user exist, capacity not full
    @Test
    public void replaceDocSuccess()  
            throws CapacityExceeded_Exception, DocDoesNotExist_Exception {
        
        sdStore.store(DOC1USER, USER, CONTENT);    
    }     

    //FAIL: replace doc content - user not exist 
    @Test(expected = UserDoesNotExist_Exception.class)
    public void replaceUserNotExist() 
            throws CapacityExceeded_Exception, DocDoesNotExist_Exception, 
            UserDoesNotExist_Exception {
        
        sdStore.store(DOCNOTEXIST, USERNOTEXIST, CONTENT);    
    }

    //FAIL: replace doc content - user exist, doc not exist
    @Test(expected = DocDoesNotExist_Exception.class)
    public void replaceDocNotExist() 
            throws CapacityExceeded_Exception, DocDoesNotExist_Exception, 
            UserDoesNotExist_Exception {
       
       sdStore.store(DOCNOTEXIST, USER, CONTENT);    
    }

    //FAIL: replace doc content - doc exist, user exist, capacity is full
    @Test(expected = CapacityExceeded_Exception.class)
    public void replaceCapacityFull() 
            throws CapacityExceeded_Exception, DocDoesNotExist_Exception, 
            UserDoesNotExist_Exception {
        
        sdStore.store(DOC1USER, USER, CONTENTFULL);    
    }

    //SUCCESS: load doc - doc exist, user exist
    @Test
    public void loadSuccess() 
            throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
        
        sdStore.load(DOC1USER, USER);    
    }     

    //FAIL: load doc - user not exist 
    @Test(expected = UserDoesNotExist_Exception.class)
    public void loadUserNotExist() 
            throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
        
        sdStore.load(DOC1USER, USERNOTEXIST);    
    }

    //FAIL: load doc - user exist, doc not exist
    @Test(expected = DocDoesNotExist_Exception.class)
    public void loadDocNotExist() 
            throws DocDoesNotExist_Exception, UserDoesNotExist_Exception {
        
        sdStore.load(DOCNOTEXIST, USER);
    }

    //SUCCESS: create doc, list user docs and load the new doc
    @Test
    public void createListAndLoad() 
            throws DocDoesNotExist_Exception, UserDoesNotExist_Exception, 
            DocAlreadyExists_Exception, CapacityExceeded_Exception, 
            UserDoesNotExist_Exception {
        
        sdStore.create(DOCNOTEXIST, USER);
        sdStore.listDocs(USER);
        sdStore.load(DOCNOTEXIST, USER);

    }

    //SUCCESS: create doc, replace the content and then load the doc
    @Test
    public void createStoreAndLoad() 
            throws DocDoesNotExist_Exception, UserDoesNotExist_Exception, 
            DocAlreadyExists_Exception, CapacityExceeded_Exception,
            UserDoesNotExist_Exception {
        
        sdStore.create(DOCNOTEXIST, USER);
        sdStore.store(DOCNOTEXIST, USER, CONTENT);
        sdStore.load(DOCNOTEXIST, USER);
    }


            //1 duzia de testes / 2 duzias de testes
            //teste exemplo: se criei, li e esta la
            //testar excepcoes
            //store com mais bytes
            //implementacao local caso seja necessario
            //mock para testar comunicacao, mock que se faz passar pelo UDDI 
    
}
