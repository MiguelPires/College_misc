package pt.tecnico.bubbledocs.integration.component;

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
import pt.tecnico.bubbledocs.exception.CannotLoadDocumentException;
import pt.tecnico.bubbledocs.exception.EmptyUsernameException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.SpreadsheetNotFoundException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;
import pt.tecnico.bubbledocs.service.BubbleDocsServiceTest;
import pt.tecnico.bubbledocs.service.ExportDocument;
import pt.tecnico.bubbledocs.service.ImportDocument;
import pt.tecnico.bubbledocs.service.integration.ImportDocumentIntegrator;
import pt.tecnico.bubbledocs.service.remote.StoreRemoteServices;

public class ImportDocumentIntegratorTest extends BubbleDocsServiceTest{
	private String ars;
    private String js;

    private static final String USERNAME = "ars";
    private static final String PASSWORD = "ars";
    private static final String EMAIL = "ars@tecnico.pt";

    private List<Spreadsheet> docs = new ArrayList<Spreadsheet>();
    private Spreadsheet full;
    private User as;
    private int ssId;

    @Override
    public void populate4Test() throws BubbleDocsException {

        as = createUser(USERNAME, EMAIL, "Antonio Rito Silva");
        as.setPassword(PASSWORD);
        ars = addUserToSession("ars");

        createUser("jshp", "1234", "João Sheepires");
        js = addUserToSession("jshp");

        docs.add(createSpreadSheet(as, "ES", 30, 20));
        docs.add(createSpreadSheet(as, "ES", 30, 20));
        
        ssId = docs.get(0).getID();

        for (Spreadsheet doc : docs) {
            doc.addCellContent(3, 4, new Literal(5));
            doc.addCellContent(5, 6, new Addition(new Literal(2), new Reference(doc.getCell(3, 4))));
        }

        /*full = createSpreadSheet(as, "FULL", 10, 10); // has around 13.000 bytes (maximum is 10*1024 = 10MB)
        for(int i=0; i<10; i++)
            for(int j=0;j<10;j++)
                full.addCellContent(i, j, new Literal(5));*/
        
        ExportDocument service = new ExportDocument(ars, docs.get(0).getID());
        service.execute();
    }
    
    @Test
    public void success() throws BubbleDocsException {
        ImportDocumentIntegrator service = new ImportDocumentIntegrator(ssId, ars);
        service.execute();

        Spreadsheet doc = service.getSpread();
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
    
    @Test(expected = SpreadsheetNotFoundException.class)
    public void spreadSheetNotExist() throws BubbleDocsException {
    	ImportDocumentIntegrator service = new ImportDocumentIntegrator(100, ars);
        service.execute();
    } 
    
    @Test(expected = UnauthorizedOperationException.class)
    public void unauthorizedImport() throws BubbleDocsException {
    	ImportDocumentIntegrator service = new ImportDocumentIntegrator(docs.get(1).getID(), js);
        service.execute();
    }
    
    @Test(expected = UserNotInSessionException.class)
    public void accessUsernameNotExist() {
        removeUserFromSession(ars);
        ImportDocumentIntegrator service = new ImportDocumentIntegrator(docs.get(0).getID(), ars);
        service.execute();
    }
    
    @Test(expected = EmptyUsernameException.class)
    public void nullUser(){
    	ImportDocumentIntegrator service = new ImportDocumentIntegrator(docs.get(0).getID(), null);
        service.execute();
    }

    @Test(expected = EmptyUsernameException.class)
    public void emptyUser(){
    	ImportDocumentIntegrator service = new ImportDocumentIntegrator(docs.get(0).getID(), "");
        service.execute();
    }
    
    @Test(expected = UnavailableServiceException.class)
    public void storeServiceUnavailable() {
        new MockUp<StoreRemoteServices>() {
            @Mock
            public byte[] loadDocument(String username, String docName) throws CannotLoadDocumentException,
                                                                             RemoteInvocationException {
                throw new RemoteInvocationException();
            }
        };
        
        ImportDocumentIntegrator service = new ImportDocumentIntegrator(docs.get(0).getID(), ars);
        service.execute();
    }
}
