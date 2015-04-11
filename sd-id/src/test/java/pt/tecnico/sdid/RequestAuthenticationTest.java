package pt.tecnico.sdid;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

import pt.ulisboa.tecnico.sdis.id.ws.AuthReqFailed_Exception;

public class RequestAuthenticationTest extends SDIdServiceTest {

    private static final String USERNAME = "alice";
    private static final String PASSWORD = "Aaa1";
    private static final byte[] PW_BYTE = PASSWORD.getBytes();
    private static final String WRONG_PASSWORD = "aaa3";
    private static final byte[] WPW_BYTE = WRONG_PASSWORD.getBytes();

    byte[] result;

    @Test
    public void success() throws AuthReqFailed_Exception {
        result = getServer().requestAuthentication(USERNAME, PW_BYTE);

        ByteBuffer buffer = ByteBuffer.allocate(4).put(result);
        assertEquals(1, buffer.getInt(0));
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
