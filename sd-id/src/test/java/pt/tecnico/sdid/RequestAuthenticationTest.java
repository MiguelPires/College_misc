package pt.tecnico.sdid;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pt.ulisboa.tecnico.sdis.id.ws.AuthReqFailed_Exception;

// WSDL contract test - Authentication Service
public class RequestAuthenticationTest extends SDIdServiceTest {

    private static final String USERNAME = "alice";
    private static final byte[] PW_BYTE = "Aaa1".getBytes();
    private static final byte[] WPW_BYTE = "aaa3".getBytes();
    private byte[] result;

    @Test
    public void success() throws AuthReqFailed_Exception {
        result = cServer.requestAuthentication(USERNAME, PW_BYTE);
        byte[] byteTrue = new byte[1];
        byteTrue[0] = (byte) 1;

        assertEquals(byteTrue[0], result[0]);
    }

    @Test(expected = AuthReqFailed_Exception.class)
    public void userDoesNotExist() throws AuthReqFailed_Exception {
        cServer.requestAuthentication("francisco", PW_BYTE);
    }

    @Test(expected = AuthReqFailed_Exception.class)
    public void wrongPassword() throws AuthReqFailed_Exception {
        cServer.requestAuthentication(USERNAME, WPW_BYTE);
    }
}
