package pt.tecnico.bubbledocs.domain;
import org.jdom2.Element;

import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;

public class Content extends Content_Base {
    
    public Content() {
        super();
    }
    
    public void delete(){
    	
    	setForbiddenCell(null);
    	deleteDomainObject();
    }
    
    public Element exportToXML() throws ShouldNotExecuteException {
        throw new ShouldNotExecuteException("exportToXML in the Content class shouldn't run.");
	}
    
    public void importFromXML(Element cellElement) throws ShouldNotExecuteException {
        throw new ShouldNotExecuteException("importToXML in the Content class shouldn't run.");
	}
}
