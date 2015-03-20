package pt.tecnico.bubbledocs.domain;

import pt.tecnico.bubbledocs.exception.ImportDocumentException;
import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;
import org.jdom2.Element;
import org.jdom2.DataConversionException;

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

    public boolean equals(Literal lit) {
        if (this.getValue().equals(lit.getValue()))
            return true;
        else
            return false;
    }
}
