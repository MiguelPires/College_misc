package pt.tecnico.bubbledocs.domain;

import org.jdom2.Element;

public class Division extends Division_Base {
    
    public Division(Argument arg1, Argument arg2) {
        super();
        setFirstOperand(arg1);
        setSecondOperand(arg2);
        
    }
    
    public Element exportToXML() {
		return exportToXML("DIV");
	}   
}
