package pt.tecnico.bubbledocs.service;

import static org.junit.Assert.assertEquals;

import javax.naming.Reference;
import javax.swing.text.AbstractDocument.Content;

import org.junit.Test;

import pt.tecnico.bubbledocs.domain.Literal;
import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.InvalidUsernameException;
import pt.tecnico.bubbledocs.exception.SpreadsheetNotFoundException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;

public class GetSpreadSheetContentTest {

	private static final String USERNAME = "jshp";
    private static final String PASSWORD = "jp#";
    private static final String EMAIL = "joao@ulisboa.pt";
    private static final String SPNAME = "Notas LEIC";
    private static final String USERNAME_DOES_NOT_EXIST = "no-one";
    private Integer ROWS = 20;
    private Integer COL = 10;
    
    private User js;
    private Spreadsheet spread;
    
    private Content content = new Literal(100);
    private Content content_1 = new Reference("test");
    private Content content_2 = new Reference("test_1");
	
	@Override
    public void populate4Test() throws BubbleDocsException {

		js = createUser(USERNAME, EMAIL, "João Pereira");

        spread = CreateSpreadSheet(USERNAME, SPNAME, ROWS, COL);
        spread.addCellContent(5, 5, content);
        spread.addCellContent(5, 6, content_1);
        spread.addCellContent(15, 10, content_2);
    }
	
	@Test
	public void success() throws BubbleDocsException {
		GetSpreadSheetContentTest service = new GetSpreadSheetContentTest(js.getUsername, USERNAME);
        service.execute();
		
        assertEquals(js.getUsername, USERNAME);
        assertEquals(content, Literal(100));
        assertEquals(content_1, Reference("test"));
        assertEquals(content_1, Reference("test_1"));
	}
	
	@Test(expected = SpreadsheetNotFoundException.class)
	public void invalidSpreadsheetIdentifier(){
		GetSpreadSheetContentTest service = new GetSpreadSheetContentTest(js.getUsername, USERNAME_DOES_NOT_EXIST);
        service.execute();
        
        assertEquals(USERNAME, "invaliId");
	}
	
	@Test(expected = UnauthorizedOperationException.class)
	public void invalidUserTokenNotAllowed(){
		GetSpreadSheetContentTest service = new GetSpreadSheetContentTest(js.getUsername, USERNAME_DOES_NOT_EXIST);
        service.execute();
        
        spread.isWriter(js.getUsername);
        spread.isReader(js.getUsername);
	}
	
	@Test(expected = SpreadsheetNotFoundException.class)
	public void invalidUserTokenNotInSession(){
		removeUserFromSession(js);
    	
    	GetSpreadSheetContentTest service = new GetSpreadSheetContentTest(js.getUsername, USERNAME);
    	
    	service.execute();
	}
	
	@Test(expected = SpreadsheetNotFoundException.class)
	public void invalidUserTokenDoesNotExist(){
    	
    	GetSpreadSheetContentTest service = new GetSpreadSheetContentTest(js.getUsername, USERNAME);
    	
    	service.execute();
    	
    	 assertEquals(js.getUsername, "notExist");
    	
	}
	
	
	
	
}
