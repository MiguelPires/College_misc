package pt.tecnico.bubbledocs.domain;

import org.jdom2.Element;

public class Cell extends Cell_Base {
    
    public Cell(int line, int column) {
        super();
        setLine(line);
        setColumn(column);
        setProtect(false);
    }
    
    public void delete(){
    	getContent().delete();
    	
    	deleteDomainObject();
    }
    
    public Element exportToXML() {
		Element element = new Element("cell");
		element.setAttribute("line",  Integer.toString(getLine()));
		element.setAttribute("column", Integer.toString(getColumn()));
		element.setAttribute("protect", ""+getProtect());
		return element;
	}
    
}
