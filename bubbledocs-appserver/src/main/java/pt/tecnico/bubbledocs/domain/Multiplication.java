package pt.tecnico.bubbledocs.domain;

import org.jdom2.Element;

public class Multiplication extends Multiplication_Base {
    
    public Multiplication() {
        super();
    }
    
    public Element exportToXML() {
		return exportToXML("MUL");
	}
    
    public void delete ()
	{
        super.delete();
	}
}
