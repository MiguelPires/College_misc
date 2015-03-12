package pt.tecnico.bubbledocs.domain;

import org.jdom2.Element;

public class Addition extends Addition_Base {
    
    public Addition() {
        super();
    }
    
    public Element exportToXML() {
		return exportToXML("ADD");
	}
	
}
