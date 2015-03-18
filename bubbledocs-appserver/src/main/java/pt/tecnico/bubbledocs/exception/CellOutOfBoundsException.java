package pt.tecnico.bubbledocs.exception;

public class CellOutOfBoundsException extends Exception{
	
	public CellOutOfBoundsException (){
		super();
	}
	
	public CellOutOfBoundsException (String message){
		super(message);
	}
}