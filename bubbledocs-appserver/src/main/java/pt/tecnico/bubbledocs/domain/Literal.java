package pt.tecnico.bubbledocs.domain;

import pt.tecnico.bubbledocs.exception.ImportDocumentException;
import org.jdom2.Element;
import org.jdom2.DataConversionException;

public class Literal extends Literal_Base {
    
    public Literal(int value) {
        super();
        setValue(value);
    }
    
    public Element exportToXML() {
		Element element = new Element("literal");
		element.setAttribute("value",  Integer.toString(getValue()));
		
		return element;
	}
	
	public void importFromXML(Element literalElement) {
		try {
			setValue(literalElement.getAttribute("value").getIntValue());
		} catch (DataConversionException e) { 
		    throw new ImportDocumentException();
		}
	    }
	
	 public void delete(){
	    	
	    	deleteDomainObject();
	    }
	 
}
