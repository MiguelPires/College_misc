package pt.tecnico.bubbledocs.domain;

import org.jdom2.Element;

public class Reference extends Reference_Base {
    
    public Reference(Cell cell) {
        super();
        setReferedCell(cell);
    }
 
    public Element exportToXML() {
		Element element = new Element("reference");
		element.setAttribute("type", "reference");
		
		Element cellElement = new Element("referedcell");
		element.addContent(cellElement);

		Cell c = getReferedCell();
		cellElement.addContent(c.exportToXML());
		
		
		return element;
	}
	
	public void importFromXML(Element referenceElement) {
	
		Element cell = referenceElement.getChild("referedcell");
		
		Element cellElement = cell.getChild("cell");
		Cell ref = new Cell(1, 1);
		ref.importFromXML(cellElement);
		setReferedCell(ref);
		
	    }
	
	public void delete ()
	{
		setReferedCell(null);
    	deleteDomainObject();

	}
}
