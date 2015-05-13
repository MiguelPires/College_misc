package pt.tecnico;

import static org.junit.Assert.*;
import pt.ulisboa.tecnico.sdis.id.ws.*;

import org.junit.Test;

public class RequestAuthenticationTest {

	private static final String USERNAME = "alice";
    private static final byte[] PW_BYTE = "Aaa1".getBytes();
    private static final byte[] WPW_BYTE = "aaa3".getBytes();
    private static final String USERNAME_DOES_NOT_EXIST = "francisco";

    private byte[] result;
    private Client client;


    @Test
    public void requestAuthSuccess() throws Exception {
        // assumes user alice exists
        client = new Client();

        result = client.requestAuthentication(USERNAME, PW_BYTE);
        assertNotNull(result);
    }
    
    @Test(expected = AuthReqFailed_Exception.class)
    public void requestAuthWrongPassword() throws Exception {
        client = new Client();

        client.requestAuthentication(USERNAME, WPW_BYTE);
    }

    @Test(expected = AuthReqFailed_Exception.class)
    public void requestAuthNullPassword() throws Exception {
        client = new Client();

        client.requestAuthentication(USERNAME, null);
    }

    @Test(expected = AuthReqFailed_Exception.class)
    public void requestAuthEmptyPassword() throws Exception {
        client = new Client();

        client.requestAuthentication(USERNAME, "".getBytes());
    }

    @Test(expected = AuthReqFailed_Exception.class)
    public void requestAuthWrongUsername() throws Exception {
        client = new Client();

        client.requestAuthentication(USERNAME_DOES_NOT_EXIST, PW_BYTE);
    }
    
    @Test(expected = AuthReqFailed_Exception.class)
    public void requestAuthNullUsername() throws Exception {
        client = new Client();

        client.requestAuthentication(null, PW_BYTE);
    }

    @Test(expected = AuthReqFailed_Exception.class)
    public void requestAuthEmptyUsername() throws Exception {
        client = new Client();

        client.requestAuthentication("", PW_BYTE);
    }
    
    @Test(expected = AuthReqFailed_Exception.class)
    public void requestAuthBothNull() throws Exception {
        client = new Client();

        client.requestAuthentication(null, null);
    }
    
    @Test(expected = AuthReqFailed_Exception.class)
    public void requestAuthBothEmpty() throws Exception {
        client = new Client();

        client.requestAuthentication("", "".getBytes());
    }
}