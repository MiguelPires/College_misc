package pt.tecnico.bubbledocs.domain;

import org.jdom2.Element;

public class BinaryFunction extends BinaryFunction_Base {
    
    public BinaryFunction(Argument one, Argument two) {
        super();
        setFirstOperand(one);
        setSecondOperand(two);
    }
    
    BinaryFunction(){}
    
    public Element exportToXML(String str) {
		Element element = new Element(str);
		
		Element firstElement = new Element("firstOperand");
		element.addContent(firstElement);

		if(getFirstOperand() instanceof Literal) 		//check class to call respective export
			firstElement.addContent(((Literal) getFirstOperand()).exportToXML());
		else
			firstElement.addContent(((Reference) getFirstOperand()).exportToXML());
		
		
		Element secondElement = new Element("secondOperand");
		element.addContent(secondElement);
		
		if(getSecondOperand() instanceof Literal)		//check class to call respective export
			secondElement.addContent(((Literal) getSecondOperand()).exportToXML());
		else
			secondElement.addContent(((Reference) getSecondOperand()).exportToXML());
	

		return element;
	}
	
	public void importFromXML(Element newElement) {
		
			
		Element first = newElement.getChild("firstOperand");
		Element second = newElement.getChild("secondOperand");

		Element firstElement = first.getChild("firstOperand");
		Argument firstArg = new Argument();

		firstArg.importFromXML(firstElement);
		setFirstOperand(firstArg);
			
		Element secondElement = second.getChild("secondOperand");
		Argument secondArg = new Argument();

		secondArg.importFromXML(secondElement);
		setSecondOperand(secondArg);
		
	}
}
