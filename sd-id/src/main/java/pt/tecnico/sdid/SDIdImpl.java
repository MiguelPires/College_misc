package pt.tecnico.sdid;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.jws.WebService;
import javax.xml.bind.JAXBElement;

import pt.ulisboa.tecnico.sdis.id.ws.AuthReqFailed;
import pt.ulisboa.tecnico.sdis.id.ws.AuthReqFailed_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.CreateUser;
import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists;
import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.ObjectFactory;
import pt.ulisboa.tecnico.sdis.id.ws.RemoveUser;
import pt.ulisboa.tecnico.sdis.id.ws.RenewPassword;
import pt.ulisboa.tecnico.sdis.id.ws.SDId;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists;
import pt.ulisboa.tecnico.sdis.id.ws.UserDoesNotExist;
import pt.ulisboa.tecnico.sdis.id.ws.UsernameProblem;

@WebService(endpointInterface = "pt.ulisboa.tecnico.sdis.id.ws.SDId", wsdlLocation = "SD-ID.1_1.wsdl", name = "SDId", portName = "SDIdImplPort", targetNamespace = "urn:pt:ulisboa:tecnico:sdis:id:ws", serviceName = "SDId")
public class SDIdImpl implements SDId {

    private int MINPASS = 1000000;
    private int MAXPASS = 9999999;
    private List<User> users;

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public void addUser(User user) throws UserAlreadyExists, EmailAlreadyExists_Exception {
        String userId = user.getUserId();
        String email = user.getEmail();
        try {
            getUser(userId); //verifies if user already exists
            UsernameProblem userProblem = new UsernameProblem();
            userProblem.setUserId(userId);
            throw new UserAlreadyExists("User " + userId + " already exists", userProblem);
        } catch (UserDoesNotExist e) {
            //User does not exist: proceed as normal 
        }
        try {
            getUserByEmail(email);
            EmailAlreadyExists emailProblem = new EmailAlreadyExists();
            emailProblem.setEmailAddress(email);
            throw new EmailAlreadyExists_Exception("Email " + email + " already exists",
                    emailProblem);
        } catch (UserDoesNotExist e) {
            //Email does not exist: proceed as normal
        }
        getUsers().add(user);
    }

    public SDIdImpl() {
        setUsers(new ArrayList<User>());
    }

    public User addUser(String username, String email, String password) throws EmailAlreadyExists_Exception,
            InvalidEmail_Exception,
            UserAlreadyExists {
        
        User user = new User(username, email, password);
        addUser(user);
        return user;
    }

    public User getUser(String userId) throws UserDoesNotExist {
        for (User user : getUsers()) {
            if (user.getUserId().equals(userId)) {
                return user;
            }
        }
        UsernameProblem userProblem = new UsernameProblem();
        userProblem.setUserId(userId);
        throw new UserDoesNotExist("User " + userId + " not found.", userProblem);
    }

    public User getUserByEmail(String email) throws UserDoesNotExist {
        for (User user : getUsers()) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        UsernameProblem userProblem = new UsernameProblem();
        userProblem.setUserId(email);
        throw new UserDoesNotExist("User with email address" + email + " not found.", userProblem);
    }

    public void createUser(CreateUser parameters) throws EmailAlreadyExists_Exception,
            InvalidEmail_Exception,
            UserAlreadyExists {

        String email = parameters.getEmailAddress();
        String userID = parameters.getUserId();

        try {
            getUserByEmail(email);

            EmailAlreadyExists emailProblem = new EmailAlreadyExists();
            emailProblem.setEmailAddress(email);
            throw new EmailAlreadyExists_Exception("The email already exists", emailProblem);
        } catch (UserDoesNotExist e) {
            // the email is unique
        }
        try {
            getUser(userID);

            UsernameProblem userProblem = new UsernameProblem();
            userProblem.setUserId(userID);
            throw new UserAlreadyExists("The user already exists", userProblem);
        } catch (UserDoesNotExist e) {
            // the user is unique
        }

        Integer randInt = new Integer(new Random().nextInt((MAXPASS - MINPASS) + 1) + MINPASS);
        String pass = randInt.toString();

        addUser(userID, email, pass);

        System.out.println("Create user - password: " + pass);
    }

    public void renewPassword(RenewPassword parameters) throws UserDoesNotExist {
        String userId = parameters.getUserId();
        User user = getUser(userId);

        Integer randInt = new Integer(new Random().nextInt((MAXPASS - MINPASS) + 1) + MINPASS);
        String pass = randInt.toString();
        user.setPassword(pass);

        System.out.println("Renew Password - password: " + pass);
    }

    public void removeUser(RemoveUser parameters) throws UserDoesNotExist {
        String userID = parameters.getUserId();
        users.remove(getUser(userID));
    }

    public byte[] requestAuthentication(String userId, byte[] reserved) throws AuthReqFailed_Exception {
        byte[] trueByte = ByteBuffer.allocate(4).putInt(1).array();
        try {
            User user = getUser(userId);
            byte[] password = user.getPassword().getBytes();
            if (Arrays.equals(reserved, password)) {
                return trueByte;
            } else {
                AuthReqFailed authProblem = new AuthReqFailed();
                JAXBElement<byte[]> element = (new ObjectFactory())
                        .createAuthReqFailedReserved(reserved);
                authProblem.setReserved(element);
                throw new AuthReqFailed_Exception("Wrong password.", authProblem);
            }
        } catch (UserDoesNotExist e) {
            byte[] userByte = userId.getBytes();
            AuthReqFailed authProblem = new AuthReqFailed();
            JAXBElement<byte[]> element = (new ObjectFactory())
                    .createAuthReqFailedReserved(userByte);
            authProblem.setReserved(element);
            throw new AuthReqFailed_Exception("User doesn't exist.", authProblem);
        }
    }
}
