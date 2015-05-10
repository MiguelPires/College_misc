package pt.tecnico.sdid;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
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

@WebService(endpointInterface = "pt.ulisboa.tecnico.sdis.id.ws.SDId", wsdlLocation = "SD-ID.1_1.wsdl", name = "SDId", portName = "SDIdImplPort", targetNamespace = "urn:pt:ulisboa:tecnico:sdis:id:ws", serviceName = "SDId")
public class SDIdImpl implements SDId {

    private int MINPASS = 1000000;
    private int MAXPASS = 9999999;
    private List<User> users;
    private CryptoHelper crypto;
    private String serverKey;

    private String getServerKey() {
        return serverKey;
    }

    public void setServerKey(String key) {
        this.serverKey = key;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public void addUser(User user) throws UserAlreadyExists_Exception, EmailAlreadyExists_Exception {
        String userId = user.getUserId();
        String email = user.getEmail();
        try {
            getUser(userId); //verifies if user already exists
            UserAlreadyExists userProblem = new UserAlreadyExists();
            userProblem.setUserId(userId);
            throw new UserAlreadyExists_Exception("User " + userId + " already exists", userProblem);
        } catch (UserDoesNotExist_Exception e) {
            //User does not exist: proceed as normal 
        }
        try {
            getUserByEmail(email);
            EmailAlreadyExists emailProblem = new EmailAlreadyExists();
            emailProblem.setEmailAddress(email);
            throw new EmailAlreadyExists_Exception("Email " + email + " already exists", emailProblem);
        } catch (UserDoesNotExist_Exception e) {
            //Email does not exist: proceed as normal
        }
        getUsers().add(user);
    }

    public User addUser(String username, String email, String password) throws EmailAlreadyExists_Exception, InvalidEmail_Exception,
                                                                       UserAlreadyExists_Exception, InvalidUser_Exception,
                                                                       NoSuchAlgorithmException, InvalidKeySpecException {

        User user = new User(username, email, password);
        addUser(user);
        return user;
    }

    public User getUser(String userId) throws UserDoesNotExist_Exception {
        for (User user : getUsers()) {
            if (user.getUserId().equals(userId)) {
                return user;
            }
        }
        UserDoesNotExist userProblem = new UserDoesNotExist();
        userProblem.setUserId(userId);
        throw new UserDoesNotExist_Exception("User " + userId + " not found.", userProblem);
    }

    public User getUserByEmail(String email) throws UserDoesNotExist_Exception {
        for (User user : getUsers()) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        UserDoesNotExist userProblem = new UserDoesNotExist();
        userProblem.setUserId(email);
        throw new UserDoesNotExist_Exception("User with email address" + email + " not found.", userProblem);
    }

    /*
     *  Constructor 
     */
    public SDIdImpl(String clientKey, String serverKey) throws EmailAlreadyExists_Exception, InvalidEmail_Exception,
                                                       UserAlreadyExists_Exception, InvalidUser_Exception {
        setUsers(new ArrayList<User>());
        this.serverKey = serverKey;
        this.crypto = new CryptoHelper("AES", "CBC", "PKCS5Padding");
    }

    /*
     * WSDL Contract Methods
     * 
     */
    public void createUser(String userId, String email) throws EmailAlreadyExists_Exception, InvalidEmail_Exception,
                                                       UserAlreadyExists_Exception, InvalidUser_Exception {
        try {
            getUserByEmail(email);

            EmailAlreadyExists emailProblem = new EmailAlreadyExists();
            emailProblem.setEmailAddress(email);
            throw new EmailAlreadyExists_Exception("The email already exists", emailProblem);
        } catch (UserDoesNotExist_Exception e) {
            // the email is unique
        }
        try {
            getUser(userId);

            UserAlreadyExists userProblem = new UserAlreadyExists();
            userProblem.setUserId(userId);
            throw new UserAlreadyExists_Exception("The user already exists", userProblem);
        } catch (UserDoesNotExist_Exception e) {
            // the user is unique
        }

        Integer randInt = new Integer(new Random().nextInt((MAXPASS - MINPASS) + 1) + MINPASS);
        String pass = randInt.toString();
        
        try {
            addUser(userId, email, pass);
        } catch(Exception e) {
            e.printStackTrace();
        }

        System.out.println("Create user - password: " + pass);
    }

    public void renewPassword(String userId) throws UserDoesNotExist_Exception {
        User user = getUser(userId);

        Integer randInt = new Integer(new Random().nextInt((MAXPASS - MINPASS) + 1) + MINPASS);
        String pass = randInt.toString();
        try {
            user.setKey(userId, pass);
        } catch (NoSuchAlgorithmException |InvalidKeySpecException e) {
            e.printStackTrace();
        }
        System.out.println("Renew Password - password: " + pass);
    }

    public void removeUser(String userId) throws UserDoesNotExist_Exception {
        users.remove(getUser(userId));
    }

    public byte[] requestAuthentication(String userId, byte[] reserved) throws AuthReqFailed_Exception {
        if (reserved == null) {
            AuthReqFailed authProblem = new AuthReqFailed();
            throw new AuthReqFailed_Exception("Byte array is null.", authProblem);
        }
        
        try {
            getUser(userId);
        } catch (UserDoesNotExist_Exception e) {
            AuthReqFailed authProblem = new AuthReqFailed();
            if (userId != null) {
                byte[] userByte = userId.getBytes();
                authProblem.setReserved(userByte);
            }
            throw new AuthReqFailed_Exception("User doesn't exist.", authProblem);
        }

        try {
            System.out.println(userId+" requested a ticket");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(reserved));
            String service = doc.getElementsByTagName("Server").item(0).getTextContent();
            String nonce = doc.getElementsByTagName("Nonce").item(0).getTextContent();

            if (service == null || service.isEmpty() || nonce == null || nonce.isEmpty())
                throw new Exception();

            TicketGranter tg = new TicketGranter(userId, service, getServerKey());
            byte[] ticket = tg.grant();

            byte[] session = createEncryptedSessionDoc(userId, builder, tg, nonce);
            System.out.println("Granting ticket to "+userId);
            return createResponse(builder, ticket, session);
        } catch (Exception e) {
            e.printStackTrace();
            AuthReqFailed authProblem = new AuthReqFailed();
            throw new AuthReqFailed_Exception("Unable to authenticate user.", authProblem);
        }
    }

    private byte[] createEncryptedSessionDoc(String userId, DocumentBuilder builder, TicketGranter tg, String nonce) throws Exception {
        Document sessionDoc = builder.newDocument();
        Element encryptedSess = sessionDoc.createElement("EncryptedSess");
        sessionDoc.appendChild(encryptedSess);

        //append children nodes
        Element sessionKey = sessionDoc.createElement("SessionKey");
        encryptedSess.appendChild(sessionKey);
        Element nonceEl = sessionDoc.createElement("Nonce");
        encryptedSess.appendChild(nonceEl);

        //append text nodes
        sessionKey.appendChild(sessionDoc.createTextNode(tg.getSessionKey()));
        nonceEl.appendChild(sessionDoc.createTextNode(nonce));

        // write to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        // transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(sessionDoc), new StreamResult(bos));
        byte[] docBytes = bos.toByteArray();

        return crypto.cipherBytes(docBytes, getUser(userId).getKey());
    }

    private byte[] createResponse(DocumentBuilder builder, byte[] ticketBytes, byte[] sessionBytes) throws TransformerException {
        Document newDoc = builder.newDocument();
        // create root node 
        Element response = newDoc.createElement("Response");
        newDoc.appendChild(response);

        // append children nodes
        Element ticket = newDoc.createElement("Ticket");
        response.appendChild(ticket);
        Element session = newDoc.createElement("Session");
        response.appendChild(session);

        String sessionString = printBase64Binary(sessionBytes);
        String ticketString = printBase64Binary(ticketBytes);

        ticket.appendChild(newDoc.createTextNode(ticketString));
        session.appendChild(newDoc.createTextNode(sessionString));

        // write to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(newDoc), new StreamResult(bos));
        return bos.toByteArray();
    }
}
