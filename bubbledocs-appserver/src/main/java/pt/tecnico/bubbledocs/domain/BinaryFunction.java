package pt.tecnico.bubbledocs.domain;

import org.jdom2.Element;

import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;

public class BinaryFunction extends BinaryFunction_Base {
    
    public BinaryFunction(Argument one, Argument two) {
        super();
        setFirstOperand(one);
        setSecondOperand(two);
    }
    
    BinaryFunction()
    {
        super();
    }
    
    @Override
    public void importFromXML(Element binElement) throws ShouldNotExecuteException {
    	
    	Element first = binElement.getChild("firstOperand");
    	Argument f = new Argument();
    	
    	f.importFromXML(first);
    	setFirstOperand(f);
    	
    	Element second = binElement.getChild("secondOperand");
    	Argument sc = new Argument();
    	
    	sc.importFromXML(second);
    	setSecondOperand(sc);
	}
    
	public void delete ()
	{
		setFirstOperand(null);
		setSecondOperand(null);
		super.delete();
	}
}
