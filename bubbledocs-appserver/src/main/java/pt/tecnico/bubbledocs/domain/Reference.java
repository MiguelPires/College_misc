package pt.tecnico.bubbledocs.domain;

import org.jdom2.Element;

import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;

public class Reference extends Reference_Base {
    
    public Reference(Cell cell) {
        super();
        setReferedCell(cell);
    }
 
    @Override
    public Element exportToXML() throws ShouldNotExecuteException {
		Element element = new Element("reference");
		
		element.addContent(getReferedCell().exportToXML());
		
		
		return element;
	}
	
    @Override
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
    	super.delete();

	}
}
