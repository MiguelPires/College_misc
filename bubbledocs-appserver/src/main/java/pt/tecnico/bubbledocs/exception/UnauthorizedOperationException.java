package pt.tecnico.bubbledocs.exception;

public class UnauthorizedOperationException extends BubbleDocsException{
	public UnauthorizedOperationException() {
        super();
    }
    
    public UnauthorizedOperationException(String message)
    {
        super(message);
    }
}
