package pt.tecnico.bubbledocs.domain;

import org.jdom2.Element;


public class Argument extends Argument_Base {
    
    public Argument() {
        super();
    }
    
    
	public void importFromXML(Element newElement) {
		
		Argument firstArg;
		
		if((newElement.getAttribute("type").getValue()).equals("literal")) //check operand class
			firstArg = new Literal(-1);
		else 
			firstArg = new Reference(new Cell(-1,-1));
				
		firstArg.importFromXML(newElement);
		    }
}
