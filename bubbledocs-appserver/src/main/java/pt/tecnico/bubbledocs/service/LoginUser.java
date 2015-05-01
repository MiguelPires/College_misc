package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.ActiveUser;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.LoginBubbleDocsException;
import pt.tecnico.bubbledocs.exception.UnknownBubbleDocsUserException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;

public class LoginUser extends BubbleDocsService {

    private String userToken;
    private String username;
    private String password;
    private User user;

    public LoginUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    protected void dispatch() throws BubbleDocsException, LoginBubbleDocsException {    
        try {
        	user = getUser();
            if (user.getPassword() == null)
                user.setPassword(password);
            else if (!user.getPassword().equals(password))
                user.setPassword(password);
            
             userToken = getBubbledocs().addUserToSession(user);         
        } catch (UnknownBubbleDocsUserException e) {
            throw new LoginBubbleDocsException();
        }
    }

    public final String getUserToken() {
        return userToken;
    }
    
    public User getUser(){
    	return getUser(username);
    }
    
    public String getUserPassword(){
    	user = getUser();
    	return user.getPassword();
    }
    
    public void deleteIfLogged(){
    	 try {
             ActiveUser loggedUser = getActiveUserByUsername(username);
             loggedUser.delete();
         } catch (UserNotInSessionException e) {
             ; // not found if user isn't already logged in 
         }
    }
    
}
