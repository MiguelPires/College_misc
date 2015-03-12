package pt.tecnico.bubbledocs.domain;

import pt.tecnico.bubbledocs.exception.ImportDocumentException;
import org.jdom2.Element;
import org.jdom2.DataConversionException;

public class Spreadsheet extends Spreadsheet_Base {

	public Spreadsheet() {
		super();
	}

	public void delete(){
		
    	for(Cell c : getCellsSet()){
    		c.delete();
    		removeCells(c);
    	}
    	
    	getCreator().removeCreatedDocs(this);
    	
    	for(User u : getWritersSet()){
    		u.removeWritableDocs(this);
    		removeWriters(u);
    	}
    	
    	for(User u : getReadersSet()){
    		u.removeReadableDocs(this);
    		removeReaders(u);
    	}
    	 
    	deleteDomainObject();
    }
	
	public Element exportToXML() {
		Element element = new Element("spreadsheet");
		element.setAttribute("ID",  Integer.toString(getID()));
		element.setAttribute("name", getName());
		//element.setAttribute("createAt", ""+getCreatedAt());
		//element.setAttribute("modifiedAt", ""+getModifiedAt());
		element.setAttribute("line",  Integer.toString(getLine()));
		element.setAttribute("column",  Integer.toString(getColumn()));
		
		Element cellElement = new Element("cell");
		element.addContent(cellElement);

		for (Cell c : getCellsSet()) {
		    cellElement.addContent(c.exportToXML());
		}
		
		return element;
	}
	
	public void importFromXML(Element spreadsheetElement) {
		
		setName(spreadsheetElement.getAttribute("name").getValue());
		//setCreatedAt(spreadsheetElement.getAttribute("createdAt").getValue());
		//setModifiedAt(spreadsheetElement.getAttribute("modifiedAt").getValue());
		
		try {
			setID(spreadsheetElement.getAttribute("ID").getIntValue());
			setLine(spreadsheetElement.getAttribute("line").getIntValue());
			setColumn(spreadsheetElement.getAttribute("column").getIntValue());
		} catch (DataConversionException e) { 
		    throw new ImportDocumentException();
		}
	    }
}
