package pt.tecnico.bubbledocs.domain;

import java.util.*;

import pt.tecnico.bubbledocs.exception.ImportDocumentException;
import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;

import org.jdom2.Element;
import org.jdom2.DataConversionException;

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
        setContent(content);
        setProtect(false);
    }
    
    @Override
    public void setForbiddenCells(Spreadsheet forbiddenCells) {
        forbiddenCells.getCell(getRow(), getColumn());
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
        for (Element el : child) {
            content = el;
            c = getXMLtype(el.getName());
        }

        try {
            c.importFromXML(content);
            setContent(c);
        } catch (ImportDocumentException e) {
            setContent(new Content());
        }

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

    public boolean equals(Cell cell) throws ShouldNotExecuteException {
        if (this.getRow().equals(cell.getRow()) && this.getColumn().equals(cell.getColumn())
                && this.getContent().equals(cell.getContent()))
            return true;

        else
            return false;
    }
}
