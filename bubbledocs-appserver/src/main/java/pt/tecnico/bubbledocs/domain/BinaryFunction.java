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
		
		
		/*Element firstElement = new Element("firstOperand");
		element.addContent(firstElement);
		Argument first = getFirstOperand();
		firstElement.addContent(first.exportToXML());
		
		Element secondElement = new Element("secondOperand");
		element.addContent(secondElement);
		Argument second = getSecondOperand();
		firstElement.addContent(second.exportToXML());*/
		

		return element;
	}
	
	public void importFromXML(Element additionElement) {
		
			
			/*Element first = additionElement.getChild("firstOperand");
			Element second = additionElement.getChild("secondOperand");
			
			Element firstElement = first.getChild("firstOperand");
			Argument firstArg = new Argument();
			firstArg.importFromXML(firstElement);
			setFirstOperand(firstArg);
			
			Element secondElement = second.getChild("secondOperand");
			Argument secondArg = new Argument();
			secondArg.importFromXML(secondElement);
			setSecondOperand(secondArg);*/
		
		    }
}
