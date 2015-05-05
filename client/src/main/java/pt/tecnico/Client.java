package pt.tecnico;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.jws.WebService;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pt.tecnico.CryptoHelper;
import pt.tecnico.frontend.SecurityHandler;
import pt.ulisboa.tecnico.sdis.id.ws.AuthReqFailed;
import pt.ulisboa.tecnico.sdis.id.ws.AuthReqFailed_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists;
import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.SDId;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.UserDoesNotExist;
import pt.ulisboa.tecnico.sdis.id.ws.UserDoesNotExist_Exception;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Map;

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
import javax.xml.ws.BindingProvider;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pt.ulisboa.tecnico.sdis.id.ws.SDId;
import pt.ulisboa.tecnico.sdis.id.ws.SDId_Service;
import uddi.UDDINaming;

public class Client {
    private static SDId cServer;
    private static String endpointAddress;
    private String nonce;
    private BindingProvider bindingProvider;
    
    public Client(String uddiURL, String serverName) throws TransformerFactoryConfigurationError,
                                                    Exception {
        UDDINaming uddiNaming = new UDDINaming(uddiURL);
        endpointAddress = uddiNaming.lookup(serverName);

        if (endpointAddress == null) {
            System.out.println("The server \"" + serverName + "\" wasn't found");
            return;
        } else {
            System.out.println("The address \"" + endpointAddress + "\" was found");
        }

        System.out.println("Creating stub");
        SDId_Service service = new SDId_Service();
        cServer = service.getSDIdImplPort();

        System.out.println("Setting endpoint address");
        bindingProvider = (BindingProvider) cServer;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();

        requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);

        byte[] credentials = cServer.requestAuthentication("alice", getRequest());
        parseCredentials(credentials);
    }

    private byte[] getRequest() throws TransformerFactoryConfigurationError,
                               ParserConfigurationException, TransformerException {
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
        String strTicket = doc.getElementsByTagName("Ticket").item(0).getTextContent();

        if (strSession == null || strSession.isEmpty() || strTicket == null || strTicket.isEmpty())
            throw new Exception("Response is empty");

        byte[] encryptedSession = parseBase64Binary(strSession);

        // switch to system.getProperty
        String CLIENT_KEY = "bH7OZp6X11DNSrBr2MBt6g==";
        
        SecretKey clientKey = crypto.decodeKey(CLIENT_KEY);
        
      /*  byte[] b = "abc".getBytes();
        byte[] c = crypto.cipherBytes(b, clientKey);
        byte[] d = crypto.decipherBytes(c, clientKey);
        System.out.println("ABCD: "+new String(d, "UTF-8"));*/

        byte[] plainBytes = crypto.decipherBytes(encryptedSession, clientKey);
        Document newDoc = builder.parse(new ByteArrayInputStream(plainBytes));

        String strSessionKey = newDoc.getElementsByTagName("SessionKey").item(0).getTextContent();
        String strNonce = newDoc.getElementsByTagName("Nonce").item(0).getTextContent();

        if (strSessionKey == null || strSessionKey.isEmpty() || strNonce == null
                || strNonce.isEmpty())
            throw new Exception("Response is empty");

        if (!this.nonce.equals(strNonce))
            throw new Exception("NONCE IS DIFFERENT");
        
    }
}
