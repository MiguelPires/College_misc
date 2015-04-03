package pt.tecnico.bubbledocs.exception;

public class CannotStoreDocumentException extends BubbleDocsException{
    
    public CannotStoreDocumentException() {
        super();
    }
    
    public CannotStoreDocumentException(String message)
    {
        super(message);
    }
}
