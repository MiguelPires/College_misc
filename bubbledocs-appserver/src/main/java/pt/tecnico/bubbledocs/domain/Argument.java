package pt.tecnico.bubbledocs.domain;

import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;


public class Argument extends Argument_Base {
    
    public Argument() {
        super();
    }
    
	public void delete ()
	{
		setForbiddenBin1(null);
		setForbiddenBin2(null);
		super.delete();
	}
	
	public boolean equals(Argument arg) throws ShouldNotExecuteException
	{
	    throw new ShouldNotExecuteException("Equals method in Argument Class");
	}
}
