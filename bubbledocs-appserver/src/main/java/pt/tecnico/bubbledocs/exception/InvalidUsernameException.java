package pt.tecnico.bubbledocs.exception;

public class InvalidUsernameException extends BubbleDocsException {
    public InvalidUsernameException() {
        super();
    }

    public InvalidUsernameException(String message) {
        super(message);
    }
}
