package pt.tecnico.bubbledocs.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pt.tecnico.bubbledocs.domain.Content;
import pt.tecnico.bubbledocs.domain.Literal;
import pt.tecnico.bubbledocs.domain.Reference;
import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.InvalidUsernameException;
import pt.tecnico.bubbledocs.exception.SpreadsheetNotFoundException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;

public class GetSpreadSheetContentTest extends BubbleDocsServiceTest{

	private static final String USERNAME = "jshp";
    private static final String PASSWORD = "jp#";
    private static final String EMAIL = "joao@ulisboa.pt";
    private static final String SPNAME = "Notas LEIC";
    private static final String USERNAME_DOES_NOT_EXIST = "no-one";
    private Integer ROWS = 20;
    private Integer COL = 10;
    
    private User js;
    private Spreadsheet spread;
    private String token;
    
    private Content content = new Literal(100);
    private Content content_1 = new Reference();
    private Content content_2 = new Reference();
	
	@Override
    public void populate4Test() throws BubbleDocsException {

		js = createUser(USERNAME, EMAIL, "João Pereira");

        spread = createSpreadSheet(js, SPNAME, ROWS, COL);
        spread.addCellContent(5, 5, content);
        spread.addCellContent(5, 6, content_1);
        spread.addCellContent(15, 10, content_2);
        
        token = addUserToSession(USERNAME);
    }
	
	@Test
	public void success() throws BubbleDocsException {
		GetSpreadSheetContentTest service = new GetSpreadSheetContent(spread.getID(), token);
        service.execute();
		
        assertEquals(js.getUsername(), USERNAME);
        assertEquals(content, Literal(100));
        assertEquals(content_1, Reference());
        assertEquals(content_1, Reference());
	}
	
	@Test(expected = SpreadsheetNotFoundException.class)
	public void invalidSpreadsheetIdentifier(){
		GetSpreadSheetContentTest service = new GetSpreadSheetContent(spread.getID()+10, token);
        service.execute();
	}
	
	@Test(expected = UnauthorizedOperationException.class)
	public void invalidUserTokenNotAllowed(){
		GetSpreadSheetContentTest service = new GetSpreadSheetContent(spread.getID(), USERNAME_DOES_NOT_EXIST);
        service.execute();
	}
	
	@Test(expected = SpreadsheetNotFoundException.class)
	public void invalidUserTokenNotInSession(){
		removeUserFromSession(js.getUsername());
    	GetSpreadSheetContentTest service = new GetSpreadSheetContent(spread.getID(), USERNAME);	
    	service.execute();
	}
	
	@Test(expected = SpreadsheetNotFoundException.class)
	public void invalidUserTokenDoesNotExist(){
    	GetSpreadSheetContentTest service = new GetSpreadSheetContent(spread.getID(), USERNAME);
    	service.execute();
    	assertEquals(js.getUsername(), "notExist");
	}
	
}
