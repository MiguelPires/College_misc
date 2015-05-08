package pt.tecnico.bubbledocs.integration.system;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;

import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.service.BubbleDocsServiceTest;
import pt.tecnico.bubbledocs.service.integration.CreateSpreadSheetIntegrator;
import pt.tecnico.bubbledocs.service.integration.DeleteUserIntegrator;
import pt.tecnico.bubbledocs.service.integration.ExportDocumentIntegrator;
import pt.tecnico.bubbledocs.service.integration.ImportDocumentIntegrator;
import pt.tecnico.bubbledocs.service.integration.LoginUserIntegrator;
import pt.tecnico.bubbledocs.service.integration.RenewPasswordIntegrator;

public class RemoteSystemIT extends BubbleDocsServiceTest {

    private String root;
    private static final String USERNAME = "alice";
    private static final String PASSWORD = "Aaa1";
    private static final Integer ROWS = 10;
    private static final Integer COLUMNS = 10;
    private static final String DOC_NAME = "Debts to the Queen of Hearts";

    public void populate() throws BubbleDocsException {

        root = addUserToSession("root");
    }

    @Override
    @After
    public void tearDown() {
        // nothing to remove from database
    }

    @Test
    public void success() {
        LoginUserIntegrator loginService = new LoginUserIntegrator(USERNAME, PASSWORD);
        loginService.execute();
        String aliceToken = loginService.getUserToken();

        User alice = getUserFromUsername("alice");
        assertEquals(aliceToken, alice.getActiveUser().getToken());
        assertEquals(USERNAME, alice.getUsername());
        assertEquals(PASSWORD, alice.getPassword());

        CreateSpreadSheetIntegrator spreadsheetService = new CreateSpreadSheetIntegrator(aliceToken, DOC_NAME, ROWS, COLUMNS);
        spreadsheetService.execute();
        int docID = spreadsheetService.getID();

        Spreadsheet doc = getSpreadSheet("Debts to the Queen of Hearts");
        assertEquals(docID, doc.getID());
        assertEquals(ROWS, doc.getRows());
        assertEquals(COLUMNS, doc.getColumns());
        assertEquals(DOC_NAME, doc.getName());

        ExportDocumentIntegrator exportService = new ExportDocumentIntegrator(aliceToken, docID);
        exportService.execute();

        ImportDocumentIntegrator importService = new ImportDocumentIntegrator(docID, aliceToken);
        importService.execute();

        RenewPasswordIntegrator renewService = new RenewPasswordIntegrator(aliceToken);
        renewService.execute();

        DeleteUserIntegrator deleteService = new DeleteUserIntegrator(root, USERNAME);
        deleteService.execute();
    }
}
