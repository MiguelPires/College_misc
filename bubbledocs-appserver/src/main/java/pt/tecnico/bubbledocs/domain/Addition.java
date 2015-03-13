package pt.tecnico.bubbledocs.domain;

import org.jdom2.Element;

public class Addition extends Addition_Base {
    
    public Addition(Argument arg1, Argument arg2) {
        super();
        setFirstOperand(arg1);
        setSecondOperand(arg2);
    }
    
    public Element exportToXML() {
		return exportToXML("ADD");
	}
	
}
