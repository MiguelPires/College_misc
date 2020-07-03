package pt.tecnico.sdid;

import org.junit.Test;

import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists_Exception;

public class CreateUserTest extends SDIdServiceTest {
    private static final String USERNAME = "bob";
    private static final String EMAIL = "bob@tecnico.pt";
    private static final String WRONG_EMAIL1 = "alice@";
    private static final String WRONG_EMAIL2 = "@tecnico.pt";

    @Test
    public void success() throws Exception {
        cServer.createUser(USERNAME, EMAIL);
    }

    @Test(expected = UserAlreadyExists_Exception.class)
    public void userAlreadyExists() throws Exception {
        cServer.createUser(USERNAME, EMAIL);
        cServer.createUser(USERNAME, "bob1@tecnico.pt");
    }

    @Test(expected = InvalidUser_Exception.class)
    public void emptyUsername() throws Exception {
        cServer.createUser("", EMAIL);
    }

    @Test(expected = InvalidUser_Exception.class)
    public void nullUsername() throws Exception {
        cServer.createUser(null, EMAIL);
    }

    @Test(expected = EmailAlreadyExists_Exception.class)
    public void emailAlreadyExists() throws Exception {
        cServer.createUser(USERNAME, EMAIL);
        cServer.createUser("bort", EMAIL);
    }

    @Test(expected = InvalidEmail_Exception.class)
    public void invalidEmailNoFirstHalf() throws Exception {
        cServer.createUser(USERNAME, WRONG_EMAIL2);
    }

    @Test(expected = InvalidEmail_Exception.class)
    public void invalidEmailNoSecondHalf() throws Exception {
        cServer.createUser(USERNAME, WRONG_EMAIL1);
    }

    @Test(expected = InvalidEmail_Exception.class)
    public void emptyEmail() throws Exception {
        cServer.createUser(USERNAME, "");
    }

    @Test(expected = InvalidEmail_Exception.class)
    public void nullEmail() throws Exception {
        cServer.createUser(USERNAME, null);
    }
}
