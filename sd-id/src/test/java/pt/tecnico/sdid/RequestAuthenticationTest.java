package pt.tecnico.sdid;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pt.ulisboa.tecnico.sdis.id.ws.AuthReqFailed_Exception;

// WSDL contract test - Authentication Service
public class RequestAuthenticationTest extends SDIdServiceTest {

    private static final String USERNAME = "alice";
    private static final byte[] PW_BYTE = "Aaa1".getBytes();
    private static final byte[] WPW_BYTE = "aaa3".getBytes();
    private byte[] result;

    @Test
    public void success() throws Exception {
        // create XML document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // create root node 
        Element request = doc.createElement("Request");
        doc.appendChild(request);
        
        // append children nodes
        Element server = doc.createElement("Server");
        request.appendChild(server);
        Element nonce = doc.createElement("Nonce");
        request.appendChild(nonce);

        // append text to children nodes
        server.appendChild(doc.createTextNode("SD-STORE"));
        
        Date d = new Date();
        nonce.appendChild(doc.createTextNode(d.toString()));
        
        // write to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(bos));
        byte[] docBytes = bos.toByteArray();
        
        result = cServer.requestAuthentication(USERNAME, docBytes);
        byte[] byteTrue = new byte[1];
        byteTrue[0] = (byte) 1;

        assertEquals(byteTrue[0], result[0]);
    }

    @Test(expected = AuthReqFailed_Exception.class)
    public void userDoesNotExist() throws AuthReqFailed_Exception {
        cServer.requestAuthentication("francisco", PW_BYTE);
    }

    @Test(expected = AuthReqFailed_Exception.class)
    public void wrongPassword() throws AuthReqFailed_Exception {
        cServer.requestAuthentication(USERNAME, WPW_BYTE);
    }
}
