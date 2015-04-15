package pt.tecnico.sdid;

import org.junit.After;
import org.junit.Test;

import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.UserDoesNotExist_Exception;

public class CreateUserTest extends SDIdServiceTest {
    private static final String USERNAME = "bob";
    private static final String EMAIL = "bob@tecnico.pt";
    private static final String WRONG_EMAIL1 = "bob@";
    private static final String WRONG_EMAIL2 = "@tecnico.pt";

    @After
    public void tearDown() throws UserDoesNotExist_Exception {
        try {
            server.removeUser(USERNAME);
        } catch (UserDoesNotExist_Exception e) {}
    }
    
    @Test
    public void success() throws Exception {
        server.createUser(USERNAME, EMAIL);
    }

    @Test(expected = UserAlreadyExists_Exception.class)
    public void userAlreadyExists() throws Exception {
        server.createUser(USERNAME, EMAIL);
        server.createUser(USERNAME, "bob1@tecnico.pt");        
    }
    
    @Test(expected = InvalidUser_Exception.class)
    public void emptyUsername() throws Exception {
        server.createUser("", EMAIL);
    }
    
    @Test(expected = InvalidUser_Exception.class)
    public void nullUsername() throws Exception {
        server.createUser(null, EMAIL);
    }

    @Test(expected = EmailAlreadyExists_Exception.class)
    public void emailAlreadyExists() throws Exception {
        server.createUser(USERNAME, EMAIL);
        server.createUser("bort", EMAIL);        
    }
    
    @Test(expected = InvalidEmail_Exception.class)
    public void invalidEmailNoFirstHalf() throws Exception {
        server.createUser(USERNAME, WRONG_EMAIL2);
    }
    
    @Test(expected = InvalidEmail_Exception.class)
    public void invalidEmailNoSecondHalf() throws Exception {
        server.createUser(USERNAME, WRONG_EMAIL1);
    }
    
    @Test(expected = InvalidEmail_Exception.class)
    public void emptyEmail() throws Exception {
        server.createUser(USERNAME, "");
    }
    
    @Test(expected = InvalidEmail_Exception.class)
    public void nullEmail() throws Exception {
        server.createUser(USERNAME, null);
    }
}
