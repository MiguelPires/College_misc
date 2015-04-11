package pt.tecnico.sdid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activity.InvalidActivityException;

import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;

public class User {

	private String userId;
	private String email;
	private String password;

	public User(String userId, String email, String password) throws InvalidEmail_Exception {
		setEmail(email);
		setUserId(userId);
		setPassword(password);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) throws InvalidEmail_Exception {
    	Pattern p = Pattern.compile("[A-Za-z0-9_\\.]@[A-Za-z0-9\\.]");
    	Matcher m = p.matcher(email);
    	if (m.matches()) {
    		this.email = email;
    	} else {
    		InvalidEmail ie = new InvalidEmail();
    		ie.setEmailAddress(email);
    		throw new InvalidEmail_Exception("Invalid e-mail address: " + email, ie);
    	}
    	
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

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
