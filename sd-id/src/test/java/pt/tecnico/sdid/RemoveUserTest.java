package pt.tecnico.sdid;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.UserDoesNotExist_Exception;

public class RemoveUserTest extends SDIdServiceTest {
    private static final String USERNAME = "bob";
    private static final String EMAIL = "bob@tecnico.pt";

    @Before
    public void setUp() throws EmailAlreadyExists_Exception, InvalidEmail_Exception, InvalidUser_Exception, UserAlreadyExists_Exception {
        server.createUser(USERNAME, EMAIL);
    }
    
    @After
    public void tearDown() {
        try {
            server.removeUser(USERNAME);
        } catch (UserDoesNotExist_Exception e) {}
    }
    
    @Test
    public void success() throws UserDoesNotExist_Exception, EmailAlreadyExists_Exception, InvalidEmail_Exception, InvalidUser_Exception, UserAlreadyExists_Exception {
        server.removeUser(USERNAME);
        server.createUser(USERNAME, EMAIL);
    }

    @Test(expected = UserDoesNotExist_Exception.class)
    public void userDoesNotExist() throws UserDoesNotExist_Exception {
        server.removeUser("Batman");   
    }
}
