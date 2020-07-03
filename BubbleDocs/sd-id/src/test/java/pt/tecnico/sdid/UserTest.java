package pt.tecnico.sdid;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists_Exception;

// Implementation test - User
public class UserTest {
    private static final String USERNAME = "francisco";
    private static final String PASSWORD = "Fff6";
    private static final String EMAIL = "francisco@tecnico.pt";
    private static final String USERNAME2 = "gonçalo";
    private static final String PASSWORD2 = "Ggg7";
    private static final String EMAIL2 = "goncalo@tecnico.pt";

    private static SDIdImpl server;

    @Before
    public void setUp() throws EmailAlreadyExists_Exception, InvalidEmail_Exception,
                       UserAlreadyExists_Exception, InvalidUser_Exception {
        server = new SDIdImpl(System.getProperty("key.client"), System.getProperty("key.server"));
    }

    @Test
    public void success() throws Exception {
        User user = server.addUser(USERNAME, EMAIL, PASSWORD);
        assertEquals(USERNAME, user.getUserId());
        assertEquals(EMAIL, user.getEmail());
      //  assertEquals(PASSWORD, user.getPassword());
    }

    @Test(expected = InvalidEmail_Exception.class)
    public void invalidEmailNoAt() throws Exception {
        server.addUser(USERNAME, "francisco.tecnico.pt", PASSWORD);
    }

    @Test(expected = InvalidEmail_Exception.class)
    public void invalidEmailNoFirstHalf() throws Exception {
        server.addUser(USERNAME, "@tecnico.pt", PASSWORD);
    }

    @Test(expected = InvalidEmail_Exception.class)
    public void invalidEmailNoSecondHalf() throws Exception {
        server.addUser(USERNAME, "francisco@", PASSWORD);
    }

    @Test(expected = EmailAlreadyExists_Exception.class)
    public void emailAlreadyExists() throws Exception {
        server.addUser(USERNAME, EMAIL, PASSWORD);
        server.addUser(USERNAME2, EMAIL, PASSWORD2);
    }

    @Test(expected = UserAlreadyExists_Exception.class)
    public void userAlreadyExists() throws Exception {
        server.addUser(USERNAME2, EMAIL2, PASSWORD2);
        server.addUser(USERNAME2, EMAIL, PASSWORD2);
    }
}
