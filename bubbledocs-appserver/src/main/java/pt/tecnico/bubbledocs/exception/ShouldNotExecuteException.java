package pt.tecnico.bubbledocs.exception;

public class ShouldNotExecuteException extends RuntimeException{
    public ShouldNotExecuteException (String message){
        super(message);
    }
}
