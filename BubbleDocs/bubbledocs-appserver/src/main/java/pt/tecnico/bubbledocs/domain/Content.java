package pt.tecnico.bubbledocs.domain;

import org.jdom2.Element;

import pt.tecnico.bubbledocs.exception.ImportDocumentException;
import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;

public class Content extends Content_Base {

    public Content() {
        super();
    }

    public void delete() {

        setForbiddenCell(null);
        deleteDomainObject();
    }

    public String print() throws ShouldNotExecuteException {
        throw new ShouldNotExecuteException("Cannot access.");
    }

    public Integer getValue() throws ShouldNotExecuteException {
        throw new ShouldNotExecuteException("Cannot access.");
    }

    public Element exportToXML() throws ShouldNotExecuteException {
        throw new ShouldNotExecuteException("exportToXML in the Content class shouldn't run.");
    }

    public void importFromXML(Element cellElement) throws ImportDocumentException {
        throw new ImportDocumentException();
    }
}
