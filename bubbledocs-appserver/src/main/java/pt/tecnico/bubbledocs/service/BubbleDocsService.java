package pt.tecnico.bubbledocs.service;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;
import pt.tecnico.bubbledocs.domain.ActiveUser;
import pt.tecnico.bubbledocs.domain.Bubbledocs;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UserNotFoundException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;

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
    
    public static User getUser(String username) throws UserNotFoundException
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
    

}
