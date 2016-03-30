package sec.blockfs.blockutility;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ServerErrorException extends Exception implements Serializable {
    public ServerErrorException() {
        super();
    }

    public ServerErrorException(String message) {
        super(message);
    }

}
