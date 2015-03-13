package pt.tecnico.bubbledocs.domain;

import org.jdom2.Element;

public class Subtraction extends Subtraction_Base {
    
    public Subtraction() {
        super();
    }
    
    public Element exportToXML() {
		return exportToXML("SUB");
	}
    
    public void delete ()
	{
        super.delete();
	}
}
