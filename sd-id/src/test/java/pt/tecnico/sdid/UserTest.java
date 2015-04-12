package pt.tecnico.sdid;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists;

public class UserTest {
	private static final String USERNAME = "francisco";
    private static final String PASSWORD = "Fff6";
    private static final String EMAIL = "francisco@tecnico.pt";
    private static final String USERNAME2 = "gonçalo";
    private static final String PASSWORD2 = "Ggg7";
    private static final String EMAIL2 = "goncalo@tecnico.pt";
    
    private static SDIdImpl server;
    
    @Before
    public void setUp(){
    	server = new SDIdImpl();
    }
    
    @Test
    public void success() throws InvalidEmail_Exception, EmailAlreadyExists_Exception, UserAlreadyExists {
    	
    	User user = server.createUser(USERNAME, EMAIL, PASSWORD);
    	assertEquals(USERNAME, user.getUserId());
    	assertEquals(EMAIL, user.getEmail());
    	assertEquals(PASSWORD, user.getPassword());
    }
    
    @Test (expected = InvalidEmail_Exception.class)
    public void invalidEmailNoAt() throws InvalidEmail_Exception, EmailAlreadyExists_Exception, UserAlreadyExists {
    	server.createUser(USERNAME, "francisco.tecnico.pt", PASSWORD);
    }
    
    @Test (expected = InvalidEmail_Exception.class)
    public void invalidEmailNoFirstHalf() throws InvalidEmail_Exception, EmailAlreadyExists_Exception, UserAlreadyExists {
    	server.createUser(USERNAME, "@tecnico.pt", PASSWORD);
    }
    
    @Test (expected = InvalidEmail_Exception.class)
    public void invalidEmailNoSecondHalf() throws InvalidEmail_Exception, EmailAlreadyExists_Exception, UserAlreadyExists {
    	server.createUser(USERNAME, "francisco@", PASSWORD);
    }
    
    @Test (expected = EmailAlreadyExists_Exception.class)
    public void emailAldreadyExists() throws EmailAlreadyExists_Exception, InvalidEmail_Exception, UserAlreadyExists {
    	server.createUser(USERNAME, EMAIL, PASSWORD);
    	server.createUser(USERNAME2, EMAIL, PASSWORD2);
    }
    
    @Test (expected = UserAlreadyExists.class)
    public void userAldreadyExists() throws UserAlreadyExists, InvalidEmail_Exception, EmailAlreadyExists_Exception {
    	server.createUser(USERNAME2, EMAIL2, PASSWORD2);
    	server.createUser(USERNAME2, EMAIL, PASSWORD2);
    }
}
