package pt.tecnico.sdid;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;

import pt.tecnico.CryptoHelper;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser_Exception;

public class User {

    private String userId;
    private String email;
    private SecretKey key;

    public User(String userId, String email, String pass) throws InvalidEmail_Exception, InvalidUser_Exception, NoSuchAlgorithmException,
                                                         InvalidKeySpecException {
        setEmail(email);
        setUserId(userId);
        setKey(userId, pass);
    }

    public SecretKey getKey() {
        return key;
    }

    private void setKey(SecretKey key) {
        this.key = key;
    }
    
    protected void setKey (String userId, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        CryptoHelper crypto = new CryptoHelper("AES", "CBC", "PKCS5Padding");
        SecretKey key = crypto.generateKeyFromPassword(printBase64Binary(password.getBytes()), userId);
        setKey(key);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) throws InvalidUser_Exception {
        if (userId == null || userId.equals("")) {
            InvalidUser userProblem = new InvalidUser();
            userProblem.setUserId(userId);
            throw new InvalidUser_Exception("The password is either null or empty", userProblem);
        }
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) throws InvalidEmail_Exception {
        if (email == null) {
            InvalidEmail emailProblem = new InvalidEmail();
            emailProblem.setEmailAddress(email);
            throw new InvalidEmail_Exception("E-mail address is null", emailProblem);
        }

        Pattern p = Pattern.compile("[A-Za-z0-9_\\.]+@[A-Za-z0-9\\.]+");
        Matcher m = p.matcher(email);
        if (m.matches()) {
            this.email = email;
        } else {
            InvalidEmail emailProblem = new InvalidEmail();
            emailProblem.setEmailAddress(email);
            throw new InvalidEmail_Exception("Invalid e-mail address: " + email, emailProblem);
        }
    }
}
