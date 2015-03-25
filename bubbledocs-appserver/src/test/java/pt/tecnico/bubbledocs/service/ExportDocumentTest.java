package pt.tecnico.bubbledocs.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import pt.tecnico.bubbledocs.domain.Addition;
import pt.tecnico.bubbledocs.domain.Cell;
import pt.tecnico.bubbledocs.domain.Literal;
import pt.tecnico.bubbledocs.domain.Reference;
import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.SpreadsheetNotFoundException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;

public class ExportDocumentTest extends BubbleDocsServiceTest {
    private String ars;
    private String js;

    private static final String USERNAME = "ars";
    private static final String PASSWORD = "ars";

    private List<Spreadsheet> docs = new ArrayList<Spreadsheet>();;
    private User as;

    @Override
    public void populate4Test() throws BubbleDocsException {

        as = createUser(USERNAME, PASSWORD, "Antonio Rito Silva");
        ars = addUserToSession("ars");

        createUser("js", "1234", "João Sheepires");
        js = addUserToSession("js");

        docs.add(createSpreadSheet(as, "ES", 30, 20));
        docs.add(createSpreadSheet(as, "ES", 30, 20));

        for (Spreadsheet doc : docs) {
            doc.addCellContent(3, 4, new Literal(5));
            doc.addCellContent(5, 6, new Addition(new Literal(2), new Reference(doc.getCell(3, 4))));
        }
    }

    @Test
    public void success() throws BubbleDocsException {

        ExportDocument service = new ExportDocument(ars, docs.get(0).getID());
        service.execute();

        Spreadsheet doc = importFromXML(service.getDocXML());
        Spreadsheet expected = docs.get(1);

        assertEquals(doc.getRows(), expected.getRows());
        assertEquals(doc.getColumns(), expected.getColumns());
        assertEquals(doc.getName(), expected.getName());
        assertEquals(doc.getAssignedCellsCount(), expected.getAssignedCellsCount());

        for (Cell cell : doc.getCellsSet()) {
            Cell expectedCell = expected.getCell(cell.getRow(), cell.getColumn());

            assertEquals(cell.getContent().getClass(), expectedCell.getContent().getClass());
            assertEquals(cell.getValue(), expectedCell.getValue());
        }
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void unauthorizedExport() throws BubbleDocsException {
        ExportDocument service = new ExportDocument(js, docs.get(1).getID());
        service.execute();
    }
    
    @Test(expected = SpreadsheetNotFoundException.class)
    public void spreadSheetNotExist() throws BubbleDocsException {
    	ExportDocument service = new ExportDocument(ars, 100);
        service.execute();
    }
    
    @Test(expected = UserNotInSessionException.class)
    public void accessUsernameNotExist() {
        removeUserFromSession(ars);
        ExportDocument service = new ExportDocument(ars, docs.get(0).getID());
        service.execute();
    }
}
