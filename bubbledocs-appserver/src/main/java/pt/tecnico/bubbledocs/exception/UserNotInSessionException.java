package pt.tecnico.bubbledocs.exception;

public class UserNotInSessionException extends RuntimeException {
    public UserNotInSessionException()
    {
        super();
    }
    
    public UserNotInSessionException (String message)
    {
        super (message);
    }

}
