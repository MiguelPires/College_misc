package pt.tecnico.sdid;

import static org.junit.Assert.*;

import org.junit.Test;

import pt.ulisboa.tecnico.sdis.id.ws.AuthReqFailed_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.SDId;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists;
import pt.tecnico.sdid.SDIdServiceTest;

public class RequestAuthenticationTest extends SDIdServiceTest {

	private static final String USERNAME = "alice";
    private static final String PASSWORD = "Aaa1";
    private static final byte[] PW_BYTE = PASSWORD.getBytes(); 
    private static final String WRONG_PASSWORD = "aaa3";
    private static final byte[] WPW_BYTE = WRONG_PASSWORD.getBytes(); 
    
    byte[] result;
	byte[] trueByte = new byte[] {(byte)1};
    
    @Test
    public void success() {   
    	try {
            result = getServer().requestAuthentication(USERNAME, PW_BYTE);
            assertEquals(result, trueByte);
        } catch (AuthReqFailed_Exception e) {
			e.printStackTrace();
		}
    }
    
    @Test(expected = AuthReqFailed_Exception.class)
    public void userDoesNotExist() throws AuthReqFailed_Exception {
    	getServer().requestAuthentication("francisco", PW_BYTE);
    }
    
    @Test(expected = AuthReqFailed_Exception.class)
    public void wrongPassword() throws AuthReqFailed_Exception {
    	getServer().requestAuthentication(USERNAME, WPW_BYTE);
    }
}
