package pt.tecnico;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.registry.JAXRException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pt.tecnico.handler.SecurityHandler;
import pt.ulisboa.tecnico.sdis.id.ws.AuthReqFailed;
import pt.ulisboa.tecnico.sdis.id.ws.AuthReqFailed_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.SDId;
import pt.ulisboa.tecnico.sdis.id.ws.SDId_Service;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.UserDoesNotExist_Exception;
import uddi.UDDINaming;

public class IDClient {
    private SDId authServer;
    private static IDClient instance;
    private SecretKey clientKey;
    
    // from the authentication server
    private String sessionKey;
    private String nonceStr;
    private Client genericClient;
    
    public static IDClient getInstance(Client gen) throws JAXRException {
        if (instance == null)
            instance = new IDClient(ClientMain.UDDI_URL, ClientMain.ID_NAME, gen);
        return instance;
    }
    
    private IDClient(String uddiURL, String serverName, Client gen) throws JAXRException {
        genericClient = gen;
        
        UDDINaming uddiNaming = new UDDINaming(uddiURL);
        String endpointAddress = uddiNaming.lookup(serverName);

        if (endpointAddress == null) {
            System.out.println("The server \"" + serverName + "\" wasn't found");
            return;
        } else {
            System.out.println("The address \"" + endpointAddress + "\" was found");
        }

        System.out.println("Creating stub");
        SDId_Service idService = new SDId_Service();
        authServer = idService.getSDIdImplPort();

        System.out.println("Setting endpoint address");

        BindingProvider bindingProvider = (BindingProvider) authServer;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);

    }

    public void createUser(String userId, String emailAddress) throws EmailAlreadyExists_Exception,
                                                              InvalidEmail_Exception,
                                                              InvalidUser_Exception,
                                                              UserAlreadyExists_Exception {
        authServer.createUser(userId, emailAddress);
    }

    public void renewPassword(String userId) throws UserDoesNotExist_Exception {
        authServer.renewPassword(userId);        
    }

    public void removeUser(String userId) throws UserDoesNotExist_Exception {
        authServer.removeUser(userId);        
    }

    public byte[] requestAuthentication(String userId, byte[] reserved)
                                                                       throws AuthReqFailed_Exception {
        if (reserved == null)
            throw new AuthReqFailed_Exception("Invalid password", (new AuthReqFailed()));

        try {
            CryptoHelper crypto = new CryptoHelper("AES", "CBC", "PKCS5Padding");
            clientKey = crypto.generateKeyFromPassword(printBase64Binary(reserved), userId);
          
            byte[] response = authServer.requestAuthentication(userId, composeMessage(userId));
            System.out.println("Requesting ticket for "+userId);

            parseCredentials(userId, response);
            System.out.println(userId+" obtained ticket");

            return response;
             
        } catch (Exception e) {
            AuthReqFailed failure = new AuthReqFailed();
            failure.setReserved(reserved);
            throw new AuthReqFailed_Exception("Could not login: "+e.getMessage(), failure);
        }
    }
    
    private byte[] composeMessage(String userId) throws ParserConfigurationException, TransformerException {
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
        Element client = doc.createElement("Client");
        request.appendChild(client);

        // append text to children nodes
        server.appendChild(doc.createTextNode(ClientMain.STORE_NAME));
        nonceStr = (new Date()).toString();
        nonce.appendChild(doc.createTextNode(nonceStr));
        client.appendChild(doc.createTextNode(userId));
        
        // write to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(bos));
        return bos.toByteArray();
    }
    
    private void parseCredentials(String userId, byte[] credentials) throws Exception  {
        CryptoHelper crypto = new CryptoHelper("AES", "CBC", "PKCS5Padding");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(new ByteArrayInputStream(credentials));

        String strSession = doc.getElementsByTagName("Session").item(0).getTextContent();
        String ticket = doc.getElementsByTagName("Ticket").item(0).getTextContent();

        if (strSession == null || strSession.isEmpty() || ticket == null || ticket.isEmpty())
            throw new Exception("Response is empty");

        byte[] encryptedSession = parseBase64Binary(strSession);
        byte[] plainBytes;
        
        try {
            plainBytes = crypto.decipherBytes(encryptedSession, clientKey);
        } catch (BadPaddingException e) {
            throw new AuthReqFailed_Exception("Wrong password (unreadable session)", null);
        }
        
        Document newDoc = builder.parse(new ByteArrayInputStream(plainBytes));

        String sessionKey = newDoc.getElementsByTagName("SessionKey").item(0).getTextContent();
        String strNonce = newDoc.getElementsByTagName("Nonce").item(0).getTextContent();

        if (sessionKey == null || sessionKey.isEmpty() || strNonce == null || strNonce.isEmpty())
            throw new AuthReqFailed_Exception("Response is empty", null);

        if (!nonceStr.equals(strNonce))
            throw new AuthReqFailed_Exception("Nonce is different", null);

        genericClient.tickets.put(userId, ticket);
        genericClient.sessionKeys.put(userId, sessionKey);
    }
}
