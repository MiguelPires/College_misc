package pt.tecnico.bubbledocs.exception;

public class EmptyUsernameException extends BubbleDocsException {
    public EmptyUsernameException() {
        super();
    }

    public EmptyUsernameException(String message) {
        super(message);
    }
}
