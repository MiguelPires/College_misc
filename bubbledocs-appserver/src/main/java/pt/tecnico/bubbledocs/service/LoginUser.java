package pt.tecnico.bubbledocs.service;

import java.util.Random;

import pt.tecnico.bubbledocs.domain.ActiveUser;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UnknownBubbleDocsUserException;
import pt.tecnico.bubbledocs.exception.UserNotFoundException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;
import pt.tecnico.bubbledocs.exception.WrongPasswordException;

// add needed import declarations

public class LoginUser extends BubbleDocsService {

    private String userToken;
    private String username;
    private String password;
    
    public LoginUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
        try{
            ActiveUser loggedUser = getActiveUserByUsername(username);
            loggedUser.delete();
        } catch (UserNotInSessionException e)
        {
           ; // not found if user isn't already logged in 
        }
            
        try {
            User user = getUser(username);

            if (!user.getPassword().equals(password))
                throw new WrongPasswordException();
            
            Random random = new Random();
            Integer randInt = new Integer(random.nextInt(10));
            
            this.userToken = this.username + randInt.toString();
            getBubbledocs().addUserToSession(user, this.userToken);

            
        } catch(UserNotFoundException e)
        {
            throw new UnknownBubbleDocsUserException();
        }
    }

    public final String getUserToken() {
        return userToken;
    }
}
