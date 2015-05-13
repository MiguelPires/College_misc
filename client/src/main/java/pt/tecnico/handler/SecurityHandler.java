package pt.tecnico.handler;


import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;

import java.io.ByteArrayOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pt.tecnico.ClientMain;
import pt.tecnico.CryptoHelper;

/**
 * This SOAPHandler outputs the contents of inbound and outbound messages.
 */
public class SecurityHandler implements SOAPHandler<SOAPMessageContext> {

    // 
    public static final String SESSION_KEY = "sessionKey";
    public static final String TICKET = "ticket";
    public static final String CLIENT = "clientName";

    public static final String TICKET_HEADER = "H_TICK";
    public static final String AUTH_HEADER = "H_AUTH";
    public static final String DIGEST_HEADER = "H_DIGEST";

    public static final String NAMESPACE = "urn:pt:ulisboa:tecnico:sdis:store:ws";
    public static final String TOKEN = "client-handler";
    public static final String INIT_SESS = "session_init";
    public static final String TYPE = "type";

    private SOAPMessageContext smc;

    public boolean handleMessage(SOAPMessageContext smc) {
        this.smc = smc;
        String address = (String) smc.get(ENDPOINT_ADDRESS_PROPERTY);
        String type = (String) smc.get(TYPE);
        Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if(type==null || !type.equals("SDID"))
            return true;
        
            if (outbound) {
                try {
                    // if ((Boolean) smc.get(INIT_SESS))
                    addTicket();
                    addAuthenticator();
                    addDigest();

                } catch (Exception e) {
                    System.out.println("Handler failed to process message outbound to SD-Store: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        

        return true;
    }

    private void addDigest() throws SOAPException, NoSuchAlgorithmException {
        if (ClientMain.HANDLER_PRINT)
            System.out.printf("Adding digest...\n");
        SOAPEnvelope se = getEnvelope();
        SOAPHeader sh = se.getHeader();

        if (sh == null)
            sh = se.addHeader();

        SOAPBody body = se.getBody();
        String message = body + (String) smc.get(SESSION_KEY);
        byte[] messageBytes = parseBase64Binary(message);

        MessageDigest hash = MessageDigest.getInstance("SHA-512");
        byte[] hashedBytes = hash.digest(messageBytes);

        String digest = printBase64Binary(hashedBytes);

        // add header element (name, namespace prefix, namespace)
        Name name = se.createName(DIGEST_HEADER, "e", NAMESPACE);
        SOAPHeaderElement element = sh.addHeaderElement(name);

        element.addTextNode(digest);
    }

    private void addTicket() throws SOAPException, NoSuchAlgorithmException {
        if (ClientMain.HANDLER_PRINT)
            System.out.printf("Adding ticket... ");

        // get token from response SOAP header
        SOAPEnvelope se = getEnvelope();
        SOAPHeader sh = se.getHeader();

        if (sh == null)
            sh = se.addHeader();

        // add header element (name, namespace prefix, namespace)
        Name name = se.createName(TICKET_HEADER, "e", NAMESPACE);
        SOAPHeaderElement element = sh.addHeaderElement(name);

        String newValue = (String) smc.get(TICKET);
        element.addTextNode(newValue);
    }

    private void addAuthenticator() throws SOAPException, ParserConfigurationException, InvalidKeyException, NoSuchAlgorithmException,
                                   NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
                                   InvalidAlgorithmParameterException, TransformerFactoryConfigurationError, TransformerException {
        if (ClientMain.HANDLER_PRINT)
            System.out.printf("Adding authenticator... ");

        SOAPEnvelope se = getEnvelope();
        SOAPHeader sh = se.getHeader();

        if (sh == null)
            sh = se.addHeader();

        Name name = se.createName(AUTH_HEADER, "e", NAMESPACE);
        SOAPHeaderElement element = sh.addHeaderElement(name);

        byte[] doc = buildXMLAuthenticator();

        CryptoHelper crypto = new CryptoHelper("AES", "CBC", "PKCS5Padding");
        SecretKey sessionKey = crypto.decodeKey((String) smc.get(SESSION_KEY));
        byte[] cipheredDoc = crypto.cipherBytes(doc, sessionKey);

        String strDoc = printBase64Binary(cipheredDoc);
        element.addTextNode(strDoc);
    }

    private byte[] buildXMLAuthenticator() throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
        // create XML document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element auth = doc.createElement("Authenticator");
        doc.appendChild(auth);

        Element client = doc.createElement("Client");
        auth.appendChild(client);

        Element timestamp = doc.createElement("Timestamp");
        auth.appendChild(timestamp);

        // append text to children nodes
        Date date = new Date();
        client.appendChild(doc.createTextNode((String) smc.get(CLIENT)));
        timestamp.appendChild(doc.createTextNode(date.toString()));

        // write XML document to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Result res = new StreamResult(bos);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        transformer.transform(new DOMSource(doc), res);
        return bos.toByteArray();
    }

    private SOAPEnvelope getEnvelope() throws SOAPException {
        SOAPMessage msg = smc.getMessage();
        SOAPPart sp = msg.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();

        return se;
    }

    public boolean handleFault(SOAPMessageContext smc) {
        return true;
    }

    // nothing to clean up
    public void close(MessageContext messageContext) {
    }

    public Set<QName> getHeaders() {
        return null;
    }
}
