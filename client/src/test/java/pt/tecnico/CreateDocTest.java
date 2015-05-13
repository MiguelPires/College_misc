package pt.tecnico;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertArrayEquals;

import javax.xml.registry.JAXRException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import pt.ulisboa.tecnico.sdis.id.ws.*;
import pt.ulisboa.tecnico.sdis.store.ws.*;

import org.junit.Test;

public class CreateDocTest {
	
	private static final String USERNAME = "alice";
    private static final byte[] PW_BYTE = "Aaa1".getBytes();
    private static final String USERNAME_B = "bruno";

    private static final String DOC_ID = "a1";
    private static final String DOC_ID_2 = "a2";
    private static final String DOC_ID_3 = "a3";
    
    private Client client;


    @Test
    public void createDocSuccess() throws Exception {
    	client = new Client();
        client.requestAuthentication(USERNAME, PW_BYTE);
        
    	final DocUserPair pair = new DocUserPair();
        pair.setDocumentId(DOC_ID);
        pair.setUserId(USERNAME);

        client.createDoc(pair);
    }

    @Test(expected = DocAlreadyExists_Exception.class)
    public void createDocTwice() throws Exception {
    	client = new Client();
        client.requestAuthentication(USERNAME, PW_BYTE);
        
    	final DocUserPair pair = new DocUserPair();
        pair.setDocumentId(DOC_ID_3);
        pair.setUserId(USERNAME);

        client.createDoc(pair);
        client.createDoc(pair);
    }

    @Test(expected = UnauthorizedOperation_Exception.class)
    public void createDocNullUser() throws Exception {
    	client = new Client();
        client.requestAuthentication(USERNAME, PW_BYTE);
        
    	final DocUserPair pair = new DocUserPair();
        pair.setDocumentId(DOC_ID_2);
        pair.setUserId(null);

        client.createDoc(pair);
    }

    @Test(expected = UnauthorizedOperation_Exception.class)
    public void createDocInvalidUser() throws Exception {
    	client = new Client();
        client.requestAuthentication(USERNAME, PW_BYTE);
        
    	final DocUserPair pair = new DocUserPair();
        pair.setDocumentId(DOC_ID_2);
        pair.setUserId(USERNAME_B);

        client.createDoc(pair);
    }

    @Test(expected = UnauthorizedOperation_Exception.class)
    public void createDocEmptyUser() throws Exception {
    	client = new Client();
        client.requestAuthentication(USERNAME, PW_BYTE);
        
    	final DocUserPair pair = new DocUserPair();
        pair.setDocumentId(DOC_ID_2);
        pair.setUserId("");

        client.createDoc(pair);
    }

    @Test(expected = InvalidArgument_Exception.class)
    public void createNullDoc() throws Exception {
    	client = new Client();
        client.requestAuthentication(USERNAME, PW_BYTE);
        
    	final DocUserPair pair = new DocUserPair();
        pair.setDocumentId(null);
        pair.setUserId(USERNAME);

        client.createDoc(pair);
    }

    @Test(expected = InvalidArgument_Exception.class)
    public void createEmptyDoc() throws Exception {
    	client = new Client();
        client.requestAuthentication(USERNAME, PW_BYTE);
        
    	final DocUserPair pair = new DocUserPair();
        pair.setDocumentId("");
        pair.setUserId(USERNAME);

        client.createDoc(pair);
    }

    @Test(expected = UnauthorizedOperation_Exception.class)
    public void createDocBothNull() throws Exception {
    	client = new Client();
        client.requestAuthentication(USERNAME, PW_BYTE);
        
    	final DocUserPair pair = new DocUserPair();
        pair.setDocumentId(null);
        pair.setUserId(null);

        client.createDoc(pair);
    }

    @Test(expected = UnauthorizedOperation_Exception.class)
    public void createDocBothEmpty() throws Exception {
    	client = new Client();
        client.requestAuthentication(USERNAME, PW_BYTE);
        
    	final DocUserPair pair = new DocUserPair();
        pair.setDocumentId("");
        pair.setUserId("");

        client.createDoc(pair);
    }

    @Test(expected = UnauthorizedOperation_Exception.class)
    public void createDocInvalidAuthentication() throws Exception {
    	client = new Client();
        client.requestAuthentication(USERNAME, PW_BYTE);
        
    	final DocUserPair pair = new DocUserPair();
        pair.setDocumentId(DOC_ID_2);
        pair.setUserId(USERNAME_B);

        client.createDoc(pair);
    }

    @Test(expected = UnauthorizedOperation_Exception.class)
    public void createDocNoAuthentication() throws Exception {
    	Client client = new Client();
        
    	final DocUserPair pair = new DocUserPair();
        pair.setDocumentId(DOC_ID);
        pair.setUserId(USERNAME);

        client.createDoc(pair);
    }

}
