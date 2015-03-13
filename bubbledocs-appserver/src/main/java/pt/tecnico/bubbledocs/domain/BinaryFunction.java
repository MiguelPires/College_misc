package pt.tecnico.bubbledocs.domain;

import org.jdom2.Element;

public class BinaryFunction extends BinaryFunction_Base {
    
    public BinaryFunction(Argument one, Argument two) {
        super();
        setFirstOperand(one);
        setSecondOperand(two);
    }
    
    BinaryFunction(){}
    
	
	public void delete ()
	{
		setFirstOperand(null);
		setSecondOperand(null);
		super.delete();
	}
}
