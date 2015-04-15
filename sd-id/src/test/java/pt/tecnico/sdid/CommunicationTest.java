package pt.tecnico.sdid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.xml.registry.JAXRException;

import mockit.Mock;
import mockit.MockUp;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import pt.tecnico.ws.uddi.UDDINaming;

// Communication test
public class CommunicationTest extends SDIdServiceTest {
    
    @BeforeClass
    public void setUpOnce() {
        ; // don't connect with the server
    }
    
    @After
    public void tearDown() {
        server = null;   
        endpointAddress = null;
    }
    
    // publishes a server instance, looks it up and checks if it's valid
    @Test
    public void success() throws Exception {
        connectToServer();

        assertEquals(serverURL, endpointAddress);
        assertNotNull(server);
    }
    
    @Test
    public void successSingleUDDIFailure() throws JAXRException {
        
        new MockUp<UDDINaming>() {
            @Mock
            public void rebind(String orgName, String url) throws JAXRException {
                throw new JAXRException();
            }
        };
        connectToServer();

        assertEquals(serverURL, endpointAddress);
        assertNotNull(server);
    }
}
