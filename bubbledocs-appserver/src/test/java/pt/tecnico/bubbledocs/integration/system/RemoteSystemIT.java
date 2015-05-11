package pt.tecnico.bubbledocs.integration.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.service.BubbleDocsServiceTest;
import pt.tecnico.bubbledocs.service.integration.CreateSpreadSheetIntegrator;
import pt.tecnico.bubbledocs.service.integration.DeleteUserIntegrator;
import pt.tecnico.bubbledocs.service.integration.ExportDocumentIntegrator;
import pt.tecnico.bubbledocs.service.integration.ImportDocumentIntegrator;
import pt.tecnico.bubbledocs.service.integration.LoginUserIntegrator;
import pt.tecnico.bubbledocs.service.integration.RenewPasswordIntegrator;

public class RemoteSystemIT extends BubbleDocsServiceTest {

    private static final String USERNAME = "alice";
    private static final String PASSWORD = "Aaa1";
    private static final Integer ROWS = 10;
    private static final Integer COLUMNS = 10;
    private static final String DOC_NAME = "Spreadsheet";

    @Test
    public void success() {
        String root = getUserFromUsername("root").getActiveUser().getToken();

        /*
         *  Login User
         */

        LoginUserIntegrator loginService = new LoginUserIntegrator(USERNAME, PASSWORD);
        loginService.execute();
        String aliceToken = loginService.getUserToken();

        User alice = getUserFromUsername("alice");
        assertEquals(aliceToken, alice.getActiveUser().getToken());
        assertEquals(USERNAME, alice.getUsername());
        assertEquals(PASSWORD, alice.getPassword());

        /*
         *  Create Spreadsheet
         */

        CreateSpreadSheetIntegrator spreadsheetService = new CreateSpreadSheetIntegrator(aliceToken, DOC_NAME, ROWS, COLUMNS);
        spreadsheetService.execute();

        int docID = spreadsheetService.getID();
        Spreadsheet doc = getSpreadSheet(DOC_NAME);
        assertEquals(docID, doc.getID());
        assertEquals(ROWS, doc.getRows());
        assertEquals(COLUMNS, doc.getColumns());
        assertEquals(DOC_NAME, doc.getName());

        /*
         *  Export Document
         */

        ExportDocumentIntegrator exportService = new ExportDocumentIntegrator(aliceToken, docID);
        exportService.execute();

        /*
         *  Import Document
         */

        ImportDocumentIntegrator importService = new ImportDocumentIntegrator(docID, aliceToken);
        importService.execute();

        Spreadsheet importedDoc = importService.getSpreadsheet();
        assertFalse(docID == importedDoc.getID());
        assertEquals(ROWS, importedDoc.getRows());
        assertEquals(COLUMNS, importedDoc.getColumns());
        assertEquals(DOC_NAME, importedDoc.getName());

        /*
         *  Renew Password
         */

        RenewPasswordIntegrator renewService = new RenewPasswordIntegrator(aliceToken);
        renewService.execute();

        User loggedOut = getUserFromSession(USERNAME);
        assertNull("User is should be logged out", loggedOut);
        alice = getUserFromUsername(USERNAME);
        assertFalse("The local password should be invalid.", alice.getValidPassword());

        /*
         *  Delete User
         */

        DeleteUserIntegrator deleteService = new DeleteUserIntegrator(root, USERNAME);
        deleteService.execute();
        User deleted = getUserFromUsername(USERNAME);
        assertNull("The user shouldn't exist", deleted);
    }
}
