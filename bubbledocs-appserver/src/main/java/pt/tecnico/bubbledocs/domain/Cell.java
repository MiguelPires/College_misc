package pt.tecnico.bubbledocs.domain;

import pt.tecnico.bubbledocs.exception.ImportDocumentException;
import org.jdom2.Element;
import org.jdom2.DataConversionException;

public class Cell extends Cell_Base {
    
    public Cell(int line, int column) {
        super();
        setLine(line);
        setColumn(column);
        setProtect(false);
    }
    
    public Cell(int line, int column, Content content)
    {
        super();
        setLine(line);
        setColumn(column);
        setContent(content);
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
		//element.setAttribute("protect", ""+getProtect());
		return element;
	}
 
    public void importFromXML(Element cellElement) {
		
		//setProtect(cellElement.getAttribute("protect").getValue());
		
		try {
			setLine(cellElement.getAttribute("line").getIntValue());
			setColumn(cellElement.getAttribute("column").getIntValue());
		} catch (DataConversionException e) { 
		    throw new ImportDocumentException();
		}
	    }
}
