package pt.tecnico.sdid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser_Exception;

public class User {

    private String userId;
    private String email;
    private String password;

    public User(String userId, String email, String password) throws InvalidEmail_Exception,
                                                             InvalidUser_Exception {
        setEmail(email);
        setUserId(userId);
        setPassword(password);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
