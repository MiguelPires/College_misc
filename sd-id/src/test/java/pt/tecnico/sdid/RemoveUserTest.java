package pt.tecnico.sdid;

import org.junit.Test;

import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.UserDoesNotExist_Exception;

public class RemoveUserTest extends SDIdServiceTest {
    private static final String USERNAME = "alice";
    private static final String EMAIL = "alice@tecnico.pt";

    @Test
    public void success() throws UserDoesNotExist_Exception, EmailAlreadyExists_Exception,
                         InvalidEmail_Exception, InvalidUser_Exception, UserAlreadyExists_Exception {
        cServer.removeUser(USERNAME);
        cServer.createUser(USERNAME, EMAIL);
    }

    @Test(expected = UserDoesNotExist_Exception.class)
    public void userDoesNotExist() throws UserDoesNotExist_Exception {
        cServer.removeUser("Batman");
    }
}
