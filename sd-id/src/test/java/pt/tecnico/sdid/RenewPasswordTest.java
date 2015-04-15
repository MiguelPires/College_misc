package pt.tecnico.sdid;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.UserDoesNotExist_Exception;

public class RenewPasswordTest extends SDIdServiceTest {

    @Before
    public void setUp() throws EmailAlreadyExists_Exception, InvalidEmail_Exception, InvalidUser_Exception, UserAlreadyExists_Exception {
        server.createUser("Coronel Kurtz", "kurtz@vietnam.com");
    }
    
    @After
    public void tearDown() throws UserDoesNotExist_Exception {
        server.removeUser("Coronel Kurtz");
    }
    
    @Test
    public void success() throws UserDoesNotExist_Exception {
        server.renewPassword("Coronel Kurtz");
    }

    @Test(expected = UserDoesNotExist_Exception.class)
    public void userDoesNotExist() throws UserDoesNotExist_Exception {
        server.renewPassword("Batman");
    }
}