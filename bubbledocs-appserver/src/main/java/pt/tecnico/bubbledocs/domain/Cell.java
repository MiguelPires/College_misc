package pt.tecnico.bubbledocs.domain;

import pt.tecnico.bubbledocs.exception.ImportDocumentException;
import org.jdom2.Element;
import org.jdom2.DataConversionException;

public class Cell extends Cell_Base {
    
    public Cell()
    {
        super();
    }
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
    	setForbiddenCells(null);
    	deleteDomainObject();
    }
    
    public Element exportToXML() {
		Element element = new Element("cell");
		element.setAttribute("line",  Integer.toString(getLine()));
		element.setAttribute("column", Integer.toString(getColumn()));
		element.setAttribute("protect", ""+getProtect());
		
		Element contentElement = new Element("content");
		element.addContent(contentElement);
		
		if(getContent() instanceof Literal){
			Literal l = (Literal) getContent();
			contentElement.addContent(l.exportToXML());
		}
		else if(getContent() instanceof Reference){
			Reference l = (Reference) getContent();
			contentElement.addContent(l.exportToXML());
		}
		
		/*else if(getContent() instanceof Addition){
			Addition l = (Addition) getContent();
			contentElement.addContent(l.exportToXML());
		}
		else if(getContent() instanceof Division){
			Division l = (Division) getContent();
			contentElement.addContent(l.exportToXML());
		}*/
		
		return element;
	}
 
    public void importFromXML(Element cellElement) {
		
        if ((cellElement.getAttribute("protect").getValue()).equals("false"))
            setProtect(false);
        else
            setProtect(true);
		
		try {
			setLine(cellElement.getAttribute("line").getIntValue());
			setColumn(cellElement.getAttribute("column").getIntValue());
		} catch (DataConversionException e) { 
		    throw new ImportDocumentException();
		}
	}
}
