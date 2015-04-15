package pt.tecnico.sdid;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;

import org.junit.Before;
import org.junit.Test;

import pt.tecnico.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.id.ws.SDId;
import pt.ulisboa.tecnico.sdis.id.ws.SDId_Service;

// Communication test
public class CommunicationTest extends SDIdServiceTest {
    
    @Before
    public void setUp() {
        // ignore setUp - server publication and lookup to be done in test
    }

    // publishes a server instance, looks it up and checks if it's valid
    @Test
    public void success() throws Exception {
        setUpServer();
        connectToServer();

        assertEquals(serverURL, endpointAddress);
        assertNotNull(server);
    }
    
}
