package pt.tecnico.bubbledocs.domain;

import org.jdom2.Element;

public class BinaryFunction extends BinaryFunction_Base {
    
    public BinaryFunction(Argument one, Argument two) {
        super();
        setFirstOperand(one);
        setSecondOperand(two);
    }
    
    BinaryFunction(){}
    
    @Override
    public void importFromXML(Element binElement) {
    	
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
