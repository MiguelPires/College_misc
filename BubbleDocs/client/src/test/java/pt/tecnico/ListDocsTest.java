package pt.tecnico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.xml.registry.JAXRException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.*;

import pt.ulisboa.tecnico.sdis.id.ws.AuthReqFailed_Exception;
import pt.ulisboa.tecnico.sdis.store.ws.*;

public class ListDocsTest {

    private static final String USERNAME = "alice";
    private static final byte[] PW_BYTE = "Aaa1".getBytes();
    private static final String USERNAME_B = "bruno";
    private static final byte[] PW_BYTE_B = "Bbb2".getBytes();
    private static final String USERNAME_DOES_NOT_EXIST = "francisco";

    private static final String DOC_ID = "a1";
    private static final String DOC_ID_2 = "a2";
    private static final String DOC_ID_3 = "a3";

    private Client client;
    
    @Test
    public void listDocsSuccess() throws Exception {
    	client = new Client();
        client.requestAuthentication(USERNAME, PW_BYTE);
        
        {
            final DocUserPair pair = new DocUserPair();
            pair.setDocumentId(DOC_ID);
            pair.setUserId(USERNAME);

            client.createDoc(pair);
        }
        {
            final DocUserPair pair = new DocUserPair();
            pair.setDocumentId(DOC_ID_2);
            pair.setUserId(USERNAME);

            client.createDoc(pair);
        }
        {
            final DocUserPair pair = new DocUserPair();
            pair.setDocumentId(DOC_ID_3);
            pair.setUserId(USERNAME);

            client.createDoc(pair);
        }

        List<String> list = client.listDocs(USERNAME);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertTrue(list.contains(DOC_ID));
        assertTrue(list.contains(DOC_ID_2));
        assertTrue(list.contains(DOC_ID_3));
    }

    @Test(expected = UnauthorizedOperation_Exception.class)
    public void listDocsUserNotInSession() throws Exception {
    	client = new Client();
        client.requestAuthentication(USERNAME, PW_BYTE);
        
        client.listDocs(USERNAME_B);
    }

    @Test(expected = UnauthorizedOperation_Exception.class)
    public void listDocsNoUser() throws Exception {
    	client = new Client();
        client.requestAuthentication(USERNAME, PW_BYTE);
        
        client.listDocs(USERNAME_DOES_NOT_EXIST);
    }

    @Test(expected = UnauthorizedOperation_Exception.class)
    public void listDocsNullUser() throws Exception {
    	client = new Client();
        client.requestAuthentication(USERNAME, PW_BYTE);
        
        client.listDocs(null);
    }

    @Test(expected = UnauthorizedOperation_Exception.class)
    public void listDocsEmptyUser() throws Exception {
    	client = new Client();
        client.requestAuthentication(USERNAME, PW_BYTE);
        
        client.listDocs("");
    }

    @Test
    public void emptyListDocs() throws Exception {
    	client = new Client();
        client.requestAuthentication(USERNAME_B, PW_BYTE_B);
        
        List<String> list = client.listDocs(USERNAME_B);
        assertNotNull(list);
        assertEquals(0, list.size());
    }

}
