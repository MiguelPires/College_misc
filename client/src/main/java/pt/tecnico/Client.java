package pt.tecnico;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pt.tecnico.handler.SecurityHandler;
import pt.ulisboa.tecnico.sdis.id.ws.SDId;
import pt.ulisboa.tecnico.sdis.store.ws.DocUserPair;

public class Client {

    private String clientKey;

    // obtained from authentication server
    private String sessionKey;
    private String ticket;
    private String nonce;

    public Client(String uddiURL, String idName, String clientKey, String storeName) throws TransformerFactoryConfigurationError, Exception {

        this.clientKey = clientKey;
        SDId idClient = IDClient.getInstance(uddiURL, idName);
        StoreClient storeClient = StoreClient.getInstance(uddiURL, storeName);
        
        storeClient.getRequestContext().put(SecurityHandler.INIT_SESS, true);

        byte[] credentials = idClient.requestAuthentication("alice", getRequest());
        parseCredentials(credentials);

        storeClient.getRequestContext().put(SecurityHandler.SESSION_KEY, this.sessionKey);
        storeClient.getRequestContext().put(SecurityHandler.TICKET, this.ticket);
        storeClient.getRequestContext().put(SecurityHandler.CLIENT, this.clientKey);

        DocUserPair du = new DocUserPair();
        du.setUserId("alice");
        du.setDocumentId("Doc1");
        storeClient.createDoc(du);
        
        du.setUserId("alice");
        du.setDocumentId("Doc2");
        storeClient.createDoc(du);
        
        List<String> docs = storeClient.listDocs("alice");
        
        for (String doc: docs) {
            System.out.println(doc);
        }
        
    }

    private byte[] getRequest() throws TransformerFactoryConfigurationError, ParserConfigurationException, TransformerException {
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
        this.nonce = d.toString();
        nonce.appendChild(doc.createTextNode(this.nonce));

        // write to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(doc), new StreamResult(bos));
        byte[] docBytes = bos.toByteArray();
        return docBytes;
    }

    private void parseCredentials(byte[] credentials) throws Exception {
        CryptoHelper crypto = new CryptoHelper("AES", "CBC", "PKCS5Padding");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(new ByteArrayInputStream(credentials));

        String strSession = doc.getElementsByTagName("Session").item(0).getTextContent();
        this.ticket = doc.getElementsByTagName("Ticket").item(0).getTextContent();

        if (strSession == null || strSession.isEmpty() || this.ticket == null || this.ticket.isEmpty())
            throw new Exception("Response is empty");

        byte[] encryptedSession = parseBase64Binary(strSession);
        SecretKey clientKey = crypto.decodeKey(this.clientKey);
        byte[] plainBytes = crypto.decipherBytes(encryptedSession, clientKey);
        Document newDoc = builder.parse(new ByteArrayInputStream(plainBytes));

        this.sessionKey = newDoc.getElementsByTagName("SessionKey").item(0).getTextContent();
        String strNonce = newDoc.getElementsByTagName("Nonce").item(0).getTextContent();

        if (this.sessionKey == null || this.sessionKey.isEmpty() || strNonce == null || strNonce.isEmpty())
            throw new Exception("Response is empty");

        if (!this.nonce.equals(strNonce))
            throw new Exception("Nonce is different");
    }
}
