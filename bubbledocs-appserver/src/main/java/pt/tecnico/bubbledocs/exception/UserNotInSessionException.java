package pt.tecnico.bubbledocs.exception;

public class UserNotInSessionException extends BubbleDocsException {
    public UserNotInSessionException()
    {
        super();
    }
    
    public UserNotInSessionException (String message)
    {
        super (message);
    }

}
