package sec.blockfs.blockutility;

public class DataIntegrityFailureException extends Exception {
    public DataIntegrityFailureException() {
        super();
    }

    public DataIntegrityFailureException(String message) {
        super(message);
    }
}
