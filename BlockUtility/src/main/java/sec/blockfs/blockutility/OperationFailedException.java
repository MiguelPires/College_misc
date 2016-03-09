package sec.blockfs.blockutility;

@SuppressWarnings("serial")
public class OperationFailedException extends
        Exception {

    public OperationFailedException(String message) {
        super(message);
    }

    public OperationFailedException() {
        super();
    }
}
