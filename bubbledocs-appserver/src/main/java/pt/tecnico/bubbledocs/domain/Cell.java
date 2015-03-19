package pt.tecnico.bubbledocs.domain;

import java.util.*;
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
		
		try{
			contentElement.addContent((getContent()).exportToXML());
		}catch (ShouldNotExecuteException e){
			;
		}
		
		
		
		return element;
	}
 
    public void importFromXML(Element cellElement) throws ImportDocumentException {
		
        if ((cellElement.getAttribute("protect").getValue()).equals("false"))
            setProtect(false);
        else
            setProtect(true);
		
        
    	Element content = cellElement.getChild("content");
    	Content c = new Content();
    	List<Element> child = content.getChildren();
    	for(Element el : child){
    		content = el;
    		c = getXMLtype(el.getName());
    	}
    	
        try{
        c.importFromXML(content);
    	setContent(c);
        }catch(ImportDocumentException e){
        	setContent(new Content());
        }
		
		try {
	    	setLine(cellElement.getAttribute("line").getIntValue());
			setColumn(cellElement.getAttribute("column").getIntValue());
		} catch (DataConversionException e) { 
		    throw new ImportDocumentException();
		}
	}
    
  //nao é a melhor maneira mas nao estou a ver como fazer doutra forma
    	public Content getXMLtype (String str){
    		if(str.equals("ADD"))
    			return new Addition();
    		else if(str.equals("SUB"))
    			return new Subtraction();
    		else if(str.equals("MUL"))
    			return new Multiplication();
    		else if(str.equals("DIV"))
    			return new Division();
    		else if(str.equals("literal"))
    			return new Literal();
    		else
    			return new Reference();    		
    	}
    	
    	public boolean equals(Cell cell) throws ShouldNotExecuteException
    	{
    	    if (this.getLine().equals(cell.getLine()) &&
    	            this.getColumn().equals(cell.getColumn()) &&
    	            this.getContent().equals(cell.getContent()))
    	        return true;
    	    
    	    else
    	        return false;
    	}

}
