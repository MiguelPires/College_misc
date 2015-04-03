package pt.tecnico.bubbledocs.exception;

public class DuplicateEmailException extends BubbleDocsException {
    public DuplicateEmailException()
    {
        super();
    }
    public DuplicateEmailException(String message)
    {
        super(message);
    }
}
