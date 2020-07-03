package sec.blockfs.blockutility;

@SuppressWarnings("serial")
public class WrongArgumentsException extends Exception {
    public WrongArgumentsException(String message) {
        super(message);
    }
    
    public WrongArgumentsException() {
        super();
    }
}
