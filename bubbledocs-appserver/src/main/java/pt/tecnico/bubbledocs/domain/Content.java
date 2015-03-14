package pt.tecnico.bubbledocs.domain;
import org.jdom2.Element;

public class Content extends Content_Base {
    
    public Content() {
        super();
    }
    
    public void delete(){
    	
    	setForbiddenCell(null);
    	deleteDomainObject();
    }
    
    public Element exportToXML() {
    	return null;
	}
    
    public void importFromXML(Element cellElement) {
		;
	}
}
