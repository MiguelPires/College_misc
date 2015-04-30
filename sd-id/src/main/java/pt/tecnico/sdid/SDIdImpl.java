package pt.tecnico.sdid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.jws.WebService;

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
            throw new EmailAlreadyExists_Exception("Email " + email + " already exists",
                    emailProblem);
        } catch (UserDoesNotExist_Exception e) {
            //Email does not exist: proceed as normal
        }
        getUsers().add(user);
    }

    public SDIdImpl() throws EmailAlreadyExists_Exception, InvalidEmail_Exception,
                     UserAlreadyExists_Exception, InvalidUser_Exception {
        setUsers(new ArrayList<User>());
    }

    public User addUser(String username, String email, String password)
                                                                       throws EmailAlreadyExists_Exception,
                                                                       InvalidEmail_Exception,
                                                                       UserAlreadyExists_Exception,
                                                                       InvalidUser_Exception {

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
        throw new UserDoesNotExist_Exception("User with email address" + email + " not found.",
                userProblem);
    }

    public void createUser(String userId, String email) throws EmailAlreadyExists_Exception,
                                                       InvalidEmail_Exception,
                                                       UserAlreadyExists_Exception,
                                                       InvalidUser_Exception {
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

        addUser(userId, email, pass);

        System.out.println("Create user - password: " + pass);
    }

    public void renewPassword(String userId) throws UserDoesNotExist_Exception {
        User user = getUser(userId);

        Integer randInt = new Integer(new Random().nextInt((MAXPASS - MINPASS) + 1) + MINPASS);
        String pass = randInt.toString();
        user.setPassword(pass);

        System.out.println("Renew Password - password: " + pass);
    }

    public void removeUser(String userId) throws UserDoesNotExist_Exception {
        users.remove(getUser(userId));
    }

    public byte[] requestAuthentication(String userId, byte[] reserved)
                                                                       throws AuthReqFailed_Exception {
        byte[] byteTrue = new byte[1];
        byteTrue[0] = (byte) 1;

        try {
            User user = getUser(userId);
            byte[] password = user.getPassword().getBytes();

            if (Arrays.equals(reserved, password)) {
                return byteTrue;
            } else {
                AuthReqFailed authProblem = new AuthReqFailed();
                authProblem.setReserved(password);
                throw new AuthReqFailed_Exception("Wrong password.", authProblem);
            }
        } catch (UserDoesNotExist_Exception e) {
            AuthReqFailed authProblem = new AuthReqFailed();
            if (userId != null) {
                byte[] userByte = userId.getBytes();
                authProblem.setReserved(userByte);
            }
            throw new AuthReqFailed_Exception("User doesn't exist.", authProblem);
        }
    }
}
