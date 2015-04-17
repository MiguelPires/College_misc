package pt.tecnico.bubbledocs.domain;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import pt.tecnico.bubbledocs.exception.ImportDocumentException;
import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;

public class Literal extends Literal_Base {

    public Literal(int value) {
        super();
        setValue(value);
    }

    public Literal() {
        super();
    }

    @Override
    public Element exportToXML() throws ShouldNotExecuteException {
        Element element = new Element("literal");
        element.setAttribute("value", Integer.toString(getValue()));

        return element;
    }

    @Override
    public void importFromXML(Element literalElement) throws ImportDocumentException {
        try {
            setValue(literalElement.getAttribute("value").getIntValue());
        } catch (DataConversionException e) {
            throw new ImportDocumentException();
        }
    }

    public void delete() {
        super.delete();
    }
}
