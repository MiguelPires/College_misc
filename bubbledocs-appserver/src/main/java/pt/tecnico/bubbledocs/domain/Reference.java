package pt.tecnico.bubbledocs.domain;

import org.jdom2.Element;

import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;
import pt.tecnico.bubbledocs.exception.ImportDocumentException;

public class Reference extends Reference_Base {
    
    public Reference(Cell cell) {
        super();
        setReferedCell(cell);
    }
    
    public Reference() {
        super();
    }
 
    @Override
    public Element exportToXML() throws ShouldNotExecuteException {
		Element element = new Element("reference");
		
		element.addContent(getReferedCell().exportToXML());
		
		
		return element;
	}
	
    @Override
	public void importFromXML(Element referenceElement) throws ImportDocumentException {
    	try{	
		Element cellElement = referenceElement.getChild("cell");
		Cell ref = new Cell(1, 1);
		ref.importFromXML(cellElement);
		setReferedCell(ref);
    	}catch (ImportDocumentException e){
    		throw new ImportDocumentException();
    	}
		
	    }
	
	public void delete ()
	{
		setReferedCell(null);
    	super.delete();

	}
}
