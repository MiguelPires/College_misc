package pt.tecnico.bubbledocs.domain;

import org.jdom2.Element;

public class Division extends Division_Base {
    
    public Division() {
        super();
    }
    
    public Element exportToXML() {
		return exportToXML("DIV");
	}   
}
