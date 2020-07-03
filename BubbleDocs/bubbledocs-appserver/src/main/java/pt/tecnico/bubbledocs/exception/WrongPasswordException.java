package pt.tecnico.bubbledocs.exception;

public class WrongPasswordException extends BubbleDocsException {
    public WrongPasswordException() {
        super();
    }

    public WrongPasswordException(String message) {
        super(message);
    }

}
