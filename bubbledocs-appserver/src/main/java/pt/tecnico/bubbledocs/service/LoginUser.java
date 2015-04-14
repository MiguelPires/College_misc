package pt.tecnico.bubbledocs.service;


import java.util.HashMap;

import pt.tecnico.bubbledocs.domain.ActiveUser;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.LoginBubbleDocsException;
import pt.tecnico.bubbledocs.exception.UnknownBubbleDocsUserException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;
import pt.tecnico.bubbledocs.exception.WrongPasswordException;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;



public class LoginUser extends BubbleDocsService {

    private String userToken;
    private String username;
    private String password;


    public LoginUser(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    
    @Override
    protected void dispatch() throws BubbleDocsException, LoginBubbleDocsException {
    	IDRemoteServices remote = new IDRemoteServices();
    
        try {
            ActiveUser loggedUser = getActiveUserByUsername(username);
            loggedUser.delete();
        } catch (UserNotInSessionException e) {
            ; // not found if user isn't already logged in 
        }
try{
User user = getUser(username);

        try {
            remote.loginUser(username,password);

            if(user.getPassword()==null)
                user.setPassword(password);
            else if (!user.getPassword().equals(password))
               user.setPassword(password);

            userToken = getBubbledocs().addUserToSession(user);
            
        } catch (RemoteInvocationException e) {
            if(user.getPassword()==null)
                throw new UnavailableServiceException();
            else if (user.getPassword().equals(password))
                userToken = getBubbledocs().addUserToSession(user);
            else
                throw new LoginBubbleDocsException();
        }
    } catch (UnknownBubbleDocsUserException e) {
            throw new LoginBubbleDocsException();
        }
    }

    public final String getUserToken() {
        return userToken;
    }
}
