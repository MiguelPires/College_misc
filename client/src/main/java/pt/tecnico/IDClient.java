package pt.tecnico;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.id.ws.AuthReqFailed_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.SDId;
import pt.ulisboa.tecnico.sdis.id.ws.SDId_Service;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.UserDoesNotExist_Exception;
import uddi.UDDINaming;

public class IDClient implements SDId{
    private SDId authServer;
    private static SDId instance;
    
    public static SDId getInstance(String uddiURL, String serverName) throws JAXRException {
        if (instance == null)
            instance = new IDClient(uddiURL, serverName);
        return instance;
    }
    private IDClient(String uddiURL, String serverName) throws JAXRException {
        UDDINaming uddiNaming = new UDDINaming(uddiURL);
        String endpointAddress = uddiNaming.lookup(serverName);

        if (endpointAddress == null) {
            System.out.println("The server \"" + serverName + "\" wasn't found");
            return;
        } else {
            System.out.println("The address \"" + endpointAddress + "\" was found");
        }

        System.out.println("Creating stub");
        SDId_Service idService = new SDId_Service();
        authServer = idService.getSDIdImplPort();

        System.out.println("Setting endpoint address");

        BindingProvider bindingProvider = (BindingProvider) authServer;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);

    }

    public void createUser(String userId, String emailAddress) throws EmailAlreadyExists_Exception,
                                                              InvalidEmail_Exception,
                                                              InvalidUser_Exception,
                                                              UserAlreadyExists_Exception {
        authServer.createUser(userId, emailAddress);
    }

    public void renewPassword(String userId) throws UserDoesNotExist_Exception {
        authServer.renewPassword(userId);        
    }

    public void removeUser(String userId) throws UserDoesNotExist_Exception {
        authServer.removeUser(userId);        
    }

    public byte[] requestAuthentication(String userId, byte[] reserved)
                                                                       throws AuthReqFailed_Exception {
        return authServer.requestAuthentication(userId, reserved);
    }
}
