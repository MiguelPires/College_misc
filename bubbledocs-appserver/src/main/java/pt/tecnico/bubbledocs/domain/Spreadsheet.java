package pt.tecnico.bubbledocs.domain;

import pt.tecnico.bubbledocs.exception.ImportDocumentException;
import org.jdom2.Element;
import org.jdom2.DataConversionException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

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
		
		/*element.setAttribute("createdYear", Integer.toString(getCreatedAt().year())));
		element.setAttribute("createdMonth", Integer.toString(getCreatedAt().monthOfYear())));
		element.setAttribute("createdDay", Integer.toString(getCreatedAt().dayOfMonth())));
		element.setAttribute("createdHour", Integer.toString(getCreatedAt().hourOfDay())));
		element.setAttribute("createdMinute", Integer.toString(getCreatedAt().minuteOfHour())));
		
		element.setAttribute("modifiedYear", Integer.toString(getModifiedAt().year())));
		element.setAttribute("modifiedMonth", Integer.toString(getModifiedAt().monthOfYear())));
		element.setAttribute("modifiedDay", Integer.toString(getModifiedAt().dayOfMonth())));
		element.setAttribute("modifiedHour", Integer.toString(getModifiedAt().hourOfDay())));
		element.setAttribute("modifiedMinute", Integer.toString(getModifiedAt().minuteOfHour())));*/
		
		element.setAttribute("createdAt", ""+getCreatedAt());
		element.setAttribute("modifiedAt", ""+getModifiedAt());
		
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
		String createTimeString = spreadsheetElement.getAttribute("createdAt").getValue();
		DateTime created = new DateTime(createTimeString, DateTimeZone.forID("Europe/Lisbon"));
		
		String modifiedTimeString = spreadsheetElement.getAttribute("modifiedAt").getValue();
		DateTime modified= new DateTime(modifiedTimeString, DateTimeZone.forID("Europe/Lisbon"));
		
		setName(spreadsheetElement.getAttribute("name").getValue());
		setCreatedAt(created);
		setModifiedAt(modified);
		
		try {
			setID(spreadsheetElement.getAttribute("ID").getIntValue());
			setLine(spreadsheetElement.getAttribute("line").getIntValue());
			setColumn(spreadsheetElement.getAttribute("column").getIntValue());
		} catch (DataConversionException e) { 
		    throw new ImportDocumentException();
		}
	    }
}
