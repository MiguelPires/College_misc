package pt.tecnico.bubbledocs.integration.system;

import org.junit.After;
import org.junit.Test;

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
    
    public void populate() throws BubbleDocsException {
        
        root = addUserToSession("root");
    }
    
    @After
    public void tearDown (){
        // nothing to remove from database
    }

    @Test
    public void success() {
        LoginUserIntegrator loginService = new LoginUserIntegrator(USERNAME, PASSWORD);
        loginService.execute();
        String aliceToken = loginService.getUserToken();
        
        CreateSpreadSheetIntegrator spreadsheetService = new CreateSpreadSheetIntegrator(aliceToken, 
                                                                     "Debts to the Queen of Hearts", 
                                                                     10, 10);
        spreadsheetService.execute();
        int docID = spreadsheetService.getID();
        
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
