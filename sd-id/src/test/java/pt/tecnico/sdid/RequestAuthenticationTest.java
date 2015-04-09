package pt.tecnico.sdid;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists;
import pt.tecnico.sdid.SDIdServiceTest;

public class RequestAuthenticationTest extends SDIdServiceTest {

    @Test
    public void success() {
        try {
            server.createUser(null);
            assertNotNull(server);
        } catch (EmailAlreadyExists_Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidEmail_Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UserAlreadyExists e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
