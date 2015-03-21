package pt.tecnico.bubbledocs.domain;

import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import pt.tecnico.bubbledocs.exception.CellOutOfBoundsException;
import pt.tecnico.bubbledocs.exception.EmptySpreadSheetNameException;
import pt.tecnico.bubbledocs.exception.ImportDocumentException;
import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;

public class Spreadsheet extends Spreadsheet_Base {

    public Spreadsheet() {
        super();
    }

    public Spreadsheet(Integer id, String name, Integer rows, Integer columns, User creator) {
        super();

        if (name.isEmpty())
            throw new EmptySpreadSheetNameException();

        setID(id);
        setName(name);
        setRows(rows);
        setColumns(columns);
        super.setCreator(creator);

        DateTime date = new DateTime(DateTimeZone.getDefault());
        setCreatedAt(date);
        setModifiedAt(date);
    }

    @Override
    public void setCreator(User creator) {
    	creator.addCreatedDocs(this);
    	super.setCreator(creator);
    }

    public Cell getCell(Integer row, Integer column) {

        if (row < 1 || row > getRows() || column < 1 || column > getColumns())
            throw new CellOutOfBoundsException();

        for (Cell cell : getCellsSet()) {
            if (cell.getRow().equals(row) && cell.getColumn().equals(column)) {
                return cell;
            }
        }

        Cell cell = new Cell(row, column);
        addCells(cell);
        return cell;
    }

    public int getAssignedCellsCount() {
        int counter = 0;
        for (Cell cell : getCellsSet()) {
            if (cell.getContent() == null)
                cell.delete();
            else
                ++counter;
        }
        return counter;
    }

    public void addCellContent(Integer row, Integer column, Content content) {
        for (Cell cell : getCellsSet()) {
            if (cell.getRow().equals(row) && cell.getColumn().equals(column)) {
                cell.setContent(content);
                return;
            }
        }
        addCells(new Cell(row, column, content));
    }

    public void deleteCellContent(Integer row, Integer column) {
        for (Cell cell : getCellsSet()) {
            if (cell.getRow().equals(row) && cell.getColumn().equals(column)) {
                cell.delete();
                removeCells(cell);
                return;
            }
        }
    }

    public boolean isWriter(String username) {
        for (User user : getWritersSet()) {
            if (user.getUsername().equals(username))
                return true;
        }
        return false;
    }

    public boolean isReader(String username) {
        for (User user : getReadersSet()) {
            if (user.getUsername().equals(username))
                return true;
        }
        return false;
    }

    public void delete() {

        for (Cell c : getCellsSet()) {
            removeCells(c);
            c.delete();
        }

        User creator = getCreator();

        try {
            creator.removeCreatedDocs(this);
        } catch (NullPointerException e) {
            // A spreadsheet foi removida pelo lado do user
        }

        super.setCreator(null);

        for (User u : getWritersSet()) {
            u.setWritableDocs(null);
            removeWriters(u);
        }

        for (User u : getReadersSet()) {
            u.setReadableDocs(null);
            removeReaders(u);
        }

        deleteDomainObject();
    }

    public Element exportToXML() throws ShouldNotExecuteException {
        Element element = new Element("spreadsheet");
        element.setAttribute("ID", Integer.toString(getID()));
        element.setAttribute("name", getName());

        element.setAttribute("createdAt", "" + getCreatedAt());
        element.setAttribute("modifiedAt", "" + getModifiedAt());

        element.setAttribute("row", Integer.toString(getRows()));
        element.setAttribute("column", Integer.toString(getColumns()));

        Element cellElement = new Element("cells");
        for (Cell c : getCellsSet()) {
            cellElement.addContent(c.exportToXML());
        }

        element.addContent(cellElement);

        Element creatorElement = new Element("creator");
        creatorElement.addContent(getCreator().exportToXML());
        element.addContent(creatorElement);

        return element;
    }

    public void importFromXML(Element spreadsheetElement, Bubbledocs app) {

        try {
            setID(app.getNewID());
            setRows(spreadsheetElement.getAttribute("row").getIntValue());
            setColumns(spreadsheetElement.getAttribute("column").getIntValue());
        } catch (DataConversionException e) {
            throw new ImportDocumentException();
        }

        String createTimeString = spreadsheetElement.getAttribute("createdAt").getValue();
        DateTime created = new DateTime(createTimeString, DateTimeZone.getDefault());

        String modifiedTimeString = spreadsheetElement.getAttribute("modifiedAt").getValue();
        DateTime modified = new DateTime(modifiedTimeString, DateTimeZone.getDefault());

        setName(spreadsheetElement.getAttribute("name").getValue());

        setCreatedAt(created);
        setModifiedAt(modified);

        Element cells = spreadsheetElement.getChild("cells");

        for (Element cell : cells.getChildren("cell")) {
            Cell c = new Cell();
            c.importFromXML(cell);
            addCells(c);
        }

        Element crt = spreadsheetElement.getChild("creator");

        User tempUser = new User();
        tempUser.importFromXML(crt.getChild("user"));
        User existingUser = app.getUser(tempUser.getUsername());
        tempUser.delete();

        existingUser.addCreatedDocs(this);
        setCreator(existingUser);
    }
}
