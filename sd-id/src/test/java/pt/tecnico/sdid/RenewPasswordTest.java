package pt.tecnico.sdid;

import org.junit.Test;

import pt.ulisboa.tecnico.sdis.id.ws.UserDoesNotExist_Exception;

public class RenewPasswordTest extends SDIdServiceTest {

    @Test
    public void success() throws UserDoesNotExist_Exception {
        cServer.renewPassword("alice");
    }

    @Test(expected = UserDoesNotExist_Exception.class)
    public void userDoesNotExist() throws UserDoesNotExist_Exception {
        cServer.renewPassword("Batman");
    }
}
