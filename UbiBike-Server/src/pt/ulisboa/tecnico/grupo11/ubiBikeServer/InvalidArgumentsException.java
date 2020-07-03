package pt.ulisboa.tecnico.grupo11.ubiBikeServer;

@SuppressWarnings("serial")
public class InvalidArgumentsException extends Exception {
    public InvalidArgumentsException(String message) {
        super(message);
    }
}
