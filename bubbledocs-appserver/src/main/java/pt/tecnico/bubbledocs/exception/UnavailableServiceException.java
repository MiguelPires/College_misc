package pt.tecnico.bubbledocs.exception;

public class UnavailableServiceException extends BubbleDocsException {
	public UnavailableServiceException() {
        super();
    }
    
    public UnavailableServiceException(String message)
    {
        super(message);
    }
}
