package pt.tecnico.bubbledocs.domain;

import java.util.List;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import pt.tecnico.bubbledocs.exception.ImportDocumentException;
import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;

public class Cell extends Cell_Base {

    public Cell() {
        super();
    }

    public Cell(int row, int column) {
        super();
        setRow(row);
        setColumn(column);
        setProtect(false);
    }

    public Cell(int row, int column, Content content) {
        super();
        setRow(row);
        setColumn(column);
        super.setContent(content);
        setProtect(false);
    }

    @Override
    public void setForbiddenCells(Spreadsheet forbiddenCells) {
        forbiddenCells.getCell(getRow(), getColumn());
    }

    @Override
    public void setContent(Content content) {
        if (this.getProtect())
            throw new UnauthorizedOperationException();
        super.setContent(content);
    }

    public void delete() {
        for (Reference c : getForbiddenReferenceSet())
            removeForbiddenReference(c);

        super.setForbiddenCells(null);

        if (getContent() != null) {
            getContent().delete();
            setContent(null);
        }

        deleteDomainObject();
    }

    public Element exportToXML() throws ShouldNotExecuteException {

        Element element = new Element("cell");
        element.setAttribute("row", Integer.toString(getRow()));
        element.setAttribute("column", Integer.toString(getColumn()));
        element.setAttribute("protect", "" + getProtect());

        Element contentElement = new Element("content");
        element.addContent(contentElement);

        contentElement.addContent((getContent()).exportToXML());

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

        content = child.get(0);
        c = getXMLtype(child.get(0).getName());

        c.importFromXML(content);
        setContent(c);

        try {
            setRow(cellElement.getAttribute("row").getIntValue());
            setColumn(cellElement.getAttribute("column").getIntValue());
        } catch (DataConversionException e) {
            throw new ImportDocumentException();
        }
    }

    public Content getXMLtype(String str) {
        if (str.equals("ADD"))
            return new Addition();
        else if (str.equals("SUB"))
            return new Subtraction();
        else if (str.equals("MUL"))
            return new Multiplication();
        else if (str.equals("DIV"))
            return new Division();
        else if (str.equals("literal"))
            return new Literal();
        else
            return new Reference();
    }

    public Integer getValue() {
        Content content = getContent();

        if (content == null)
            return null;
        else
            return content.getValue();
    }
}
