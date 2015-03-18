package pt.tecnico.bubbledocs.exception;

public class UnknownBubbleDocsUserException extends RuntimeException{
    
    public UnknownBubbleDocsUserException()
    {
        super();
    }
    public UnknownBubbleDocsUserException(String message)
    {
        super(message);
    }

}
