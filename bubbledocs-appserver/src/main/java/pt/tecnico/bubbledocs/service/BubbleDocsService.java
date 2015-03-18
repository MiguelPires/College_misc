package pt.tecnico.bubbledocs.service;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;
import pt.tecnico.bubbledocs.domain.ActiveUser;
import pt.tecnico.bubbledocs.domain.Bubbledocs;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;
import pt.tecnico.bubbledocs.exception.UnknownBubbleDocsUserException;

// add needed import declarations

public abstract class BubbleDocsService {

    @Atomic
    public final void execute() throws BubbleDocsException {
        dispatch();
    }

    protected abstract void dispatch() throws BubbleDocsException;
    
    public static Bubbledocs getBubbledocs ()
    {
        return FenixFramework.getDomainRoot().getBubbledocs();
    }
    
    public static User createUser(String username, String name, String password) {
        return Bubbledocs.getInstance().createUser(username, name, password);
    }
    
    public static User getUser(String username) throws UnknownBubbleDocsUserException
    {
        return getBubbledocs().findUser(username);
    }
    
    public static User getUserByToken(String token) throws UserNotInSessionException
    {
        return getBubbledocs().getUserByToken(token);
    }
    
    public static ActiveUser getActiveUserByUsername(String username) throws UserNotInSessionException
    {
        return getBubbledocs().getActiveUserByUsername(username);
    }
    
    public static String addUserToSession(String username) throws UnknownBubbleDocsUserException {
    	return getBubbledocs().addUserToSession(username);
    }
    
    public static void removeUserByToken(String token) throws UserNotInSessionException{
    	getBubbledocs().removeUserByToken(token);
    }
    
    public static Spreadsheet createSpreadSheet(User user, String name, int row, int column) {
        
        return getBubbledocs().createSpreadSheet(user, name, row, column);
    }
    
    public static Spreadsheet getSpreadsheet (String name)
    {
        return getBubbledocs().getSpreadSheet(name);
    }
}
   
