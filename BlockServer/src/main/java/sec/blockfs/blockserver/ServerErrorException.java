package sec.blockfs.blockserver;

@SuppressWarnings("serial")
public class ServerErrorException extends Exception {
    public ServerErrorException() {
        super();
    }

    public ServerErrorException(String message) {
        super(message);
    }

}
