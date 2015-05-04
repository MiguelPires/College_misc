package pt.tecnico.bubbledocs.integration.system;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//import javax.xml.registry.JAXRException;

import mockit.Mock;
import mockit.MockUp;

import org.junit.After;
import org.junit.Test;

import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.service.BubbleDocsService;
import pt.tecnico.bubbledocs.service.BubbleDocsServiceTest;
import pt.tecnico.bubbledocs.service.integration.CreateSpreadSheetIntegrator;
import pt.tecnico.bubbledocs.service.integration.DeleteUserIntegrator;
import pt.tecnico.bubbledocs.service.integration.ExportDocumentIntegrator;
import pt.tecnico.bubbledocs.service.integration.ImportDocumentIntegrator;
import pt.tecnico.bubbledocs.service.integration.LoginUserIntegrator;
import pt.tecnico.bubbledocs.service.integration.RenewPasswordIntegrator;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;

public class LocalSystemTest extends BubbleDocsServiceTest {

	private String root;
    private static final String USERNAME = "alice";
    private static final String PASSWORD = "Aaa1";
    
    public void populate() throws BubbleDocsException {
        
        root = addUserToSession("root");
    }
    
    @After
    public void tearDown (){
    	super.tearDown();
    }
    
    @Test
    public void success() {
    	new MockUp<IDRemoteServices>(){
    		@Mock
            private Collection<String> queryAll(String orgName) throws JAXRException {
            	List<String> result = new ArrayList<String>();
            	result.add(USERNAME);
            	return result;
            }; 
    		
    	};
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
