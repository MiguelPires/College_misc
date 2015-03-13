package pt.tecnico.bubbledocs.domain;

import org.jdom2.Element;

public class Multiplication extends Multiplication_Base {
    
    public Multiplication() {
        super();
    }
    
    @Override
    public Element exportToXML() {
		Element element = new Element("MUL");
		
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
