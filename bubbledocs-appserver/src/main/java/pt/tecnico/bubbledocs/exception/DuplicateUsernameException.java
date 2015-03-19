package pt.tecnico.bubbledocs.exception;

public class DuplicateUsernameException extends BubbleDocsException {
    public DuplicateUsernameException()
    {
        super();
    }
    public DuplicateUsernameException(String message)
    {
        super(message);
    }
}
