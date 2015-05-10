package pt.ulisboa.tecnico.sdis.store.ws.handler;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

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
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import pt.tecnico.CryptoHelper;
import pt.ulisboa.tecnico.sdis.store.ws.UnauthorizedOperation;
import pt.ulisboa.tecnico.sdis.store.ws.UnauthorizedOperation_Exception;

/**
 * This SOAPHandler outputs the contents of inbound and outbound messages.
 */
public class SecurityHandler implements SOAPHandler<SOAPMessageContext> {

    public static final String NAMESPACE = "urn:pt:ulisboa:tecnico:sdis:store:ws";
    public static final String TOKEN = "client-handler";
    public static final String SERVER_KEY = "server_key";
    public static final String SERVICE_NAME = "service_name";
    public static final String SESSION_KEY = "session_key";
    public static final String TICKET_HEADER = "H_TICK";
    public static final String AUTH_HEADER = "H_AUTH";
    public static final String DIGEST_HEADER = "H_DIGEST";
    public static final String INIT_SESS = "session_init";
    public static final String CLIENT = "clientName";

    private SOAPMessageContext smc;
    private String sessionKey;
    private static final String serverKey = "CYd/FbnCGtfTyr8uzJKeAw==";
    private static final String serviceName = "SD-STORE";
    
    public boolean handleMessage(SOAPMessageContext smc) {
        this.smc = smc;
        Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (!outbound) {
            try {
                //   if ((Boolean) smc.get(INIT_SESS))
                processTicket();
                processAuthenticator();
                processDigest();

            } catch (Exception e) {
                System.out.println("Handler failed to process message inbound from client");
            }
        }
        return true;
    }

    private void processDigest() throws Exception {
        System.out.println("Processing digest...");
        SOAPEnvelope se = getEnvelope();
        SOAPHeader sh = se.getHeader();

        if (sh == null)
            sh = se.addHeader();

        Name name = se.createName(DIGEST_HEADER, "e", NAMESPACE);
        Iterator it = sh.getChildElements(name);

        // check header element
        if (!it.hasNext()) {
            System.out.printf("Header element %s not found.%n", AUTH_HEADER);
            throw new Exception("Header not found");
        }

        SOAPElement element = (SOAPElement) it.next();
        String strDigest = element.getValue();

        SOAPBody body = se.getBody();
        String message = body + (String) smc.get(SESSION_KEY);
        byte[] messageBytes = parseBase64Binary(message);

        MessageDigest hash = MessageDigest.getInstance("SHA-512");
        byte[] hashedBytes = hash.digest(messageBytes);

        String digest = printBase64Binary(hashedBytes);

        if (!digest.equals(strDigest))
            throw new Exception("The digest isn't valid");
    }

    private void processAuthenticator() throws Exception {
        System.out.printf("Processing authenticator... ");
        SOAPEnvelope se = getEnvelope();
        SOAPHeader sh = se.getHeader();

        if (sh == null)
            sh = se.addHeader();

        Name name = se.createName(AUTH_HEADER, "e", NAMESPACE);
        Iterator it = sh.getChildElements(name);

        // check header element
        if (!it.hasNext()) {
            System.out.printf("Header element %s not found.%n", AUTH_HEADER);
            throw new Exception("Header not found");
        }
        
        SOAPElement element = (SOAPElement) it.next();
        String strAuth = element.getValue();
        Document doc = buildAuthenticator(strAuth);
        validateAuthenticator(doc);
    }

    private void validateAuthenticator(Document doc) throws Exception {
        String client = doc.getElementsByTagName("Client").item(0).getTextContent();
        String timestamp = doc.getElementsByTagName("Timestamp").item(0).getTextContent();

        if (client == null || client.isEmpty() || timestamp == null || timestamp.isEmpty())
            throw new Exception("Response is empty");

        DateFormat f = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        Date d = f.parse(timestamp);
        Date now = new Date();
        Date comp = new Date();
        comp.setTime(d.getTime() + 30000);

        if (now.compareTo(comp) > 0)
            throw new Exception("Time limit exceeded for authenticator");
    }

    private Document buildAuthenticator(String strAuth) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                                                       IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
                                                       ParserConfigurationException, SAXException, IOException {

        byte[] cipheredBytes = parseBase64Binary(strAuth);

        // decrypt authenticator
        CryptoHelper crypto = new CryptoHelper("AES", "CBC", "PKCS5Padding");
        SecretKey sessionKey = crypto.decodeKey(this.sessionKey);
        byte[] plainAuth = crypto.decipherBytes(cipheredBytes, sessionKey);


        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(plainAuth));
    }

    /**
     * Check the MESSAGE_OUTBOUND_PROPERTY in the context to see if this is an outgoing or
     * incoming message. Write a brief message to the print stream and output the message.
     * The writeTo() method can throw SOAPException or IOException
     * 
     * @throws Exception
     */
    private void processTicket() throws Exception {
        System.out.printf("Processing ticket... ");
        SOAPEnvelope se = getEnvelope();
        SOAPHeader sh = se.getHeader();

        if (sh == null)
            sh = se.addHeader();

        // get first header element
        Name name = se.createName(TICKET_HEADER, "e", NAMESPACE);
        Iterator it = sh.getChildElements(name);

        // check header element
        if (!it.hasNext()) {
            System.out.printf("Header element %s not found.%n", TICKET_HEADER);
            throw new Exception("Header not found");
        }

        SOAPElement element = (SOAPElement) it.next();

        // get header element value
        String stringTicket = element.getValue();
        Document ticket = buildXMLTicket(stringTicket);
        validateTicket(ticket);
        smc.put(SESSION_KEY, this.sessionKey);
        smc.setScope(SESSION_KEY, Scope.APPLICATION);
    }

    private Document buildXMLTicket(String stringTicket) throws Exception {
        // decrypt ticket
        CryptoHelper crypto = new CryptoHelper("AES", "CBC", "PKCS5Padding");

        SecretKey key = crypto.decodeKey(serverKey);

        byte[] cipheredTicket = parseBase64Binary(stringTicket);
        byte[] plainTicket = crypto.decipherBytes(cipheredTicket, key);

        // build ticket
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(plainTicket));
    }

    private void validateTicket(Document doc) throws Exception {
        // verify ticket data 
        String server = doc.getElementsByTagName("Server").item(0).getTextContent();

        if (!server.equals(serviceName))
            throw new Exception("The service name is incorrect");

        String initialTime = doc.getElementsByTagName("BeginsAt").item(0).getTextContent();

        Date presentDate = new Date();
        DateFormat f = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        Date initialDate = f.parse(initialTime);

        if (presentDate.compareTo(initialDate) < 0)
            throw new Exception("The present time is earlier than the initial time");

        String finalTime = doc.getElementsByTagName("ExpiresAt").item(0).getTextContent();
        Date endingDate = f.parse(finalTime);

        if (presentDate.compareTo(endingDate) > 0)
            throw new Exception("The present time is greater than the final time");

        this.sessionKey = doc.getElementsByTagName("SessionKey").item(0).getTextContent();
        String client = doc.getElementsByTagName("Client").item(0).getTextContent();
        
        smc.put(CLIENT, client);
        smc.setScope(CLIENT, Scope.APPLICATION);
    }

    private SOAPEnvelope getEnvelope() throws SOAPException {
        // get token from response SOAP header
        // get SOAP envelope header
        SOAPMessage msg = smc.getMessage();
        SOAPPart sp = msg.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        SOAPHeader sh = se.getHeader();

        return se;
    }

    public boolean handleFault(SOAPMessageContext smc) {
        // logToSystemOut(smc);
        return true;
    }

    // nothing to clean up
    public void close(MessageContext messageContext) {
    }

    public Set<QName> getHeaders() {
        return null;
    }
}
