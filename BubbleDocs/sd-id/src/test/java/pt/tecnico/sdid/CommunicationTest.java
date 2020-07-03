package pt.tecnico.sdid;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.xml.registry.JAXRException;

import mockit.Mock;
import mockit.MockUp;

import org.junit.Before;
import org.junit.Test;

import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists_Exception;
import uddi.UDDINaming;

// Communication test
public class CommunicationTest extends SDIdServiceTest {
    SDIdMain server;

    @Override
    @Before
    public void setUp() throws JAXRException, EmailAlreadyExists_Exception, InvalidEmail_Exception,
                       UserAlreadyExists_Exception, InvalidUser_Exception {
        ; // don't create server
    }

    @Override
    public void tearDown() throws JAXRException {
        super.tearDown();
        server.getEndpoint().stop();
    }

    // publishes a server instance, looks it up and checks if it's valid
    @Test
    public void success() throws Exception {
        String[] args = new String[3];
        args[0] = uddiURL;
        args[1] = serverName;
        args[2] = serverURL;

        server = new SDIdMain();
        SDIdMain.setUp(args);
    }

    @Test
    public void successUDDIFailure() throws JAXRException, EmailAlreadyExists_Exception,
                                    InvalidEmail_Exception, UserAlreadyExists_Exception,
                                    IOException, InvalidUser_Exception, NoSuchAlgorithmException, InvalidKeySpecException {

        new MockUp<UDDINaming>() {
            @Mock
            public void rebind(String orgName, String url) throws JAXRException {
                throw new JAXRException();
            }
        };
        String[] args = new String[3];
        args[0] = uddiURL;
        args[1] = serverName;
        args[2] = serverURL;

        server = new SDIdMain();
        SDIdMain.setUp(args);
    }
}
