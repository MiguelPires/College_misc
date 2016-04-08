package sec.blockfs.blockserver;

@SuppressWarnings("serial")
public class WrongArgumentsException extends Exception {
    public WrongArgumentsException(String message) {
        super(message);
    }
    
    public WrongArgumentsException() {
        super();
    }
}
