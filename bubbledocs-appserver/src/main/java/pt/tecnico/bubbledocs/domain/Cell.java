package pt.tecnico.bubbledocs.domain;

import pt.tecnico.bubbledocs.exception.ImportDocumentException;
import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;

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
    	for(Reference c : getForbiddenReferenceSet())
    		removeForbiddenReference(c);
    	
    	setForbiddenCells(null);
    	
    	if (getContent() != null)
    	{
    	    getContent().delete();
    	    setContent(null);
    	}
    	    

    	deleteDomainObject();
    }
    
    public Element exportToXML() throws ShouldNotExecuteException {
    	
		Element element = new Element("cell");
		element.setAttribute("line",  Integer.toString(getLine()));
		element.setAttribute("column", Integer.toString(getColumn()));
		element.setAttribute("protect", ""+getProtect());
		
		Element contentElement = new Element("content");
		element.addContent(contentElement);
		
		//nunca devia ser null (para os nossos exemplos)
		if(getContent() != null)
			contentElement.addContent((getContent()).exportToXML());
		
		return element;
	}
 
    public void importFromXML(Element cellElement) {
		
        if ((cellElement.getAttribute("protect").getValue()).equals("false"))
            setProtect(false);
        else
            setProtect(true);
		
        //ERRO AQUI
        
    	/*Element content = cellElement.getChild("content");
        Content c = new Content();
    	c.importFromXML(content);
    	setContent(c);*/
    	
		
		try {
			setLine(cellElement.getAttribute("line").getIntValue());
			setColumn(cellElement.getAttribute("column").getIntValue());
		} catch (DataConversionException e) { 
		    throw new ImportDocumentException();
		}
	}
}
