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
import pt.tecnico.bubbledocs.service.integration.ExportDocumentIntegrator;
import pt.tecnico.bubbledocs.service.integration.ImportDocumentIntegrator;
import pt.tecnico.bubbledocs.service.remote.StoreRemoteServices;

public class ImportDocumentIntegratorTest extends BubbleDocsServiceTest {
	private String alice; //ars;
    private String bruno; //js;

    private static final String USERNAME = "alice";
    private static final String PASSWORD = "Aaa1";
    private static final String EMAIL = "alice@tecnico.pt";

    private List<Spreadsheet> docs = new ArrayList<Spreadsheet>();
    private Spreadsheet full;
    private User userAlice; //as;
    private User userBruno; 
    private int ssId;

    @Override
    public void populate4Test() throws BubbleDocsException {

    	userAlice = createUser(USERNAME, EMAIL, "Antonio Rito Silva");
    	userAlice.setPassword(PASSWORD);
        alice = addUserToSession("alice");

        userBruno = createUser("bruno", "bruno@tecnico.pt", "Bruno Sheepires");
        userBruno.setPassword("Bbb2");
        bruno = addUserToSession("bruno");

        docs.add(createSpreadSheet(userAlice, "ES", 30, 20));
        docs.add(createSpreadSheet(userAlice, "ES", 30, 20));
        
        ssId = docs.get(0).getID();

        for (Spreadsheet doc : docs) {
            doc.addCellContent(3, 4, new Literal(5));
            doc.addCellContent(5, 6, new Addition(new Literal(2), new Reference(doc.getCell(3, 4))));
        }

        ExportDocumentIntegrator service = new ExportDocumentIntegrator(alice, docs.get(0).getID());
        service.execute();
    }
    
    @Test
    public void success() throws BubbleDocsException {
        ImportDocumentIntegrator service = new ImportDocumentIntegrator(ssId, alice);
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
    
    @Test(expected = SpreadsheetNotFoundException.class)
    public void spreadSheetNotExist() throws BubbleDocsException {
    	ImportDocumentIntegrator service = new ImportDocumentIntegrator(100, alice);
        service.execute();
    } 
    
    //alterar excepção???
    @Test(expected = UnauthorizedOperationException.class)
    public void unauthorizedImport() throws BubbleDocsException {
    	ImportDocumentIntegrator service = new ImportDocumentIntegrator(docs.get(1).getID(), bruno);
        service.execute();
    }
    
    @Test(expected = UserNotInSessionException.class)
    public void accessUsernameNotExist() {
        removeUserFromSession(alice);
        ImportDocumentIntegrator service = new ImportDocumentIntegrator(docs.get(0).getID(), alice);
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
        
        ImportDocumentIntegrator service = new ImportDocumentIntegrator(docs.get(0).getID(), alice);
        service.execute();
    }
}
