package sec.blockfs.blocklibrary;

public class InitializationFailureException extends Exception {
    public InitializationFailureException () {
        super();
    }
    
    public InitializationFailureException (String message) {
        super(message);
    }
}
