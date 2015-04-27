package pt.tecnico.bubbledocs.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import mockit.Mock;
import mockit.MockUp;

import org.junit.Test;

import pt.tecnico.bubbledocs.domain.Addition;
import pt.tecnico.bubbledocs.domain.Cell;
import pt.tecnico.bubbledocs.domain.Literal;
import pt.tecnico.bubbledocs.domain.Reference;
import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.CannotStoreDocumentException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.SpreadsheetNotFoundException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;
import pt.tecnico.bubbledocs.exception.EmptyUsernameException;
import pt.tecnico.bubbledocs.service.remote.StoreRemoteServices;
import pt.tecnico.bubbledocs.service.BubbleDocsServiceTest;

public class ExportDocumentIntegratorTest extends BubbleDocsServiceTest {
    private String ars;
    private String js;

    private static final String USERNAME = "ars";
    private static final String PASSWORD = "ars";
    private static final String EMAIL = "ars@tecnico.pt";

    private List<Spreadsheet> docs = new ArrayList<Spreadsheet>();
    private Spreadsheet full;
    private User as;

    @Override
    public void populate4Test() throws BubbleDocsException {

        as = createUser(USERNAME, EMAIL, "Antonio Rito Silva");
        as.setPassword(PASSWORD);
        ars = addUserToSession("ars");

        createUser("jshp", "1234", "João Sheepires");
        js = addUserToSession("jshp");

        docs.add(createSpreadSheet(as, "ES", 30, 20));
        docs.add(createSpreadSheet(as, "ES", 30, 20));

        for (Spreadsheet doc : docs) {
            doc.addCellContent(3, 4, new Literal(5));
            doc.addCellContent(5, 6, new Addition(new Literal(2), new Reference(doc.getCell(3, 4))));
        }

        full = createSpreadSheet(as, "FULL", 10, 10); // has around 13.000 bytes (maximum is 10*1024 = 10MB)
        for(int i=0; i<10; i++)
            for(int j=0;j<10;j++)
                full.addCellContent(i, j, new Literal(5));
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

    @Test(expected = UnavailableServiceException.class)
    public void storeServiceUnavailable() {
        new MockUp<StoreRemoteServices>() {
            @Mock
            public void storeDocument(String username, String docName, byte[] document)
                                                                                       throws CannotStoreDocumentException,
                                                                                       RemoteInvocationException {
                throw new RemoteInvocationException();
            }
        };
        
        ExportDocument service = new ExportDocument(ars, docs.get(0).getID());
        service.execute();
    }

    @Test(expected = CannotStoreDocumentException.class)
    public void storeIsFull() {
        ExportDocument service = new ExportDocument(ars, full.getID());
        service.execute();
    }

    @Test(expected = EmptyUsernameException.class)
    public void nullUser(){
        ExportDocument service = new ExportDocument(null, docs.get(0).getID());
        service.execute();
    }

    @Test(expected = EmptyUsernameException.class)
    public void emptyUser(){
        ExportDocument service = new ExportDocument("", docs.get(0).getID());
        service.execute();
    }

}
