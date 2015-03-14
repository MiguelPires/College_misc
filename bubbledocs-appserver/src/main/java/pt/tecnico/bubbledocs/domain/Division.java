package pt.tecnico.bubbledocs.domain;

import org.jdom2.Element;

import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;

public class Division extends Division_Base {
    
    public Division(Argument arg1, Argument arg2) {
        super();
        setFirstOperand(arg1);
        setSecondOperand(arg2);
        
    }
    
    @Override
    public Element exportToXML() throws ShouldNotExecuteException {
		Element element = new Element("DIV");
		
		Element firstElement = new Element("firstOperand");
		element.addContent(firstElement);
		
		firstElement.addContent((getFirstOperand()).exportToXML());
		
		Element secondElement = new Element("secondOperand");
		element.addContent(secondElement);		
		
		secondElement.addContent((getSecondOperand()).exportToXML());
		
		return element;
	}
    
    public void delete ()
	{
		super.delete();
	}
}
