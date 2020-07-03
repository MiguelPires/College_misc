package pt.tecnico.bubbledocs.service.remote;

import javax.xml.registry.JAXRException;

import pt.tecnico.Client;
import pt.tecnico.bubbledocs.exception.DuplicateEmailException;
import pt.tecnico.bubbledocs.exception.DuplicateUsernameException;
import pt.tecnico.bubbledocs.exception.InvalidEmailException;
import pt.tecnico.bubbledocs.exception.InvalidUsernameException;
import pt.tecnico.bubbledocs.exception.LoginBubbleDocsException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnknownBubbleDocsUserException;
import pt.ulisboa.tecnico.sdis.id.ws.AuthReqFailed_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.EmailAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidEmail_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.InvalidUser_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.UserAlreadyExists_Exception;
import pt.ulisboa.tecnico.sdis.id.ws.UserDoesNotExist_Exception;


public class IDRemoteServices {

    public void createUser(String username, String email) {
        try {
            Client.getInstanceID().createUser(username, email);
        } catch (JAXRException e) {
            throw new RemoteInvocationException(e.getMessage());
        } catch (InvalidEmail_Exception e) {
            throw new InvalidEmailException(e.getMessage());
        } catch (InvalidUser_Exception e) {
            throw new InvalidUsernameException(e.getMessage());
        } catch (EmailAlreadyExists_Exception e) {
            throw new DuplicateEmailException(e.getMessage());
        } catch (UserAlreadyExists_Exception e) {
            throw new DuplicateUsernameException(e.getMessage());
        }
    }

    public void loginUser(String username, String password) {
        try {
            Client.getInstanceID().requestAuthentication(username, password.getBytes());
        } catch (JAXRException e) {
            throw new RemoteInvocationException();
        } catch (AuthReqFailed_Exception e) {
            throw new LoginBubbleDocsException();
        }
    }

    public void removeUser(String username) {
        try {
            Client.getInstanceID().removeUser(username);
        } catch (JAXRException e) {
            throw new RemoteInvocationException();
        } catch (UserDoesNotExist_Exception e) {
            throw new UnknownBubbleDocsUserException(e.getMessage());
        }
    }

    public void renewPassword(String username) {
        try {
            Client.getInstanceID().renewPassword(username);
        } catch (JAXRException e) {
            throw new RemoteInvocationException();
        } catch (UserDoesNotExist_Exception e) {
            throw new UnknownBubbleDocsUserException(e.getMessage());
        }
    }
}
