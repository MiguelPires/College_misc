package pt.tecnico.bubbledocs.exception;

public class InvalidEmailException extends BubbleDocsException {
    public InvalidEmailException() {
        super();
    }

    public InvalidEmailException(String message) {
        super(message);
    }
}
