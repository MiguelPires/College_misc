package pt.tecnico.bubbledocs.domain;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import pt.tecnico.bubbledocs.exception.UserNotInSessionException;

public class Session extends Session_Base {
    
    public Session() {
        super();
    }
    
    public void addUser (User user, String token)
    {
        ActiveUser loggedUser = new ActiveUser(user, token, this);
        addActiveUsers(loggedUser);
        
        for (ActiveUser u: getActiveUsersSet())
        {
            if (isInactive(u))
                removeUser(u);
        }
    }
    
    public void removeUser(ActiveUser user)
    {       
        removeActiveUsers(user);
        user.delete();
    }
    
    public void removeUser(String token) throws UserNotInSessionException
    {       
    	for (ActiveUser user: getActiveUsersSet())
    	{
    		if (user.getToken().equals(token)){
    			user.delete();
    			//removeActiveUsers(user);
    			return;
    		}
    	}
    }
    
    public boolean isInactive(ActiveUser user)
    {
        DateTime last = user.getLastAccess();
        DateTime now = new DateTime(DateTimeZone.getDefault());
        
        if (now.compareTo(last.plusHours(2)) >= 0)
            return true;
        else 
            return false;
    }

    public User getUserByToken(String token) throws UserNotInSessionException {
        for (ActiveUser user: getActiveUsersSet())
        {
            if (user.getToken().equals(token))
                return user.getLoggedUser();
        }
        
        throw new UserNotInSessionException();
    }
    
    public ActiveUser getActiveUserByUsername (String username) throws UserNotInSessionException
    {
        for (ActiveUser user: getActiveUsersSet())
        {
            if (user.getLoggedUser().getUsername().equals(username))
                return user;
        }
        throw new UserNotInSessionException();
    }
}
