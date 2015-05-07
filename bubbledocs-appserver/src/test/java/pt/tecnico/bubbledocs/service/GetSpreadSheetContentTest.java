package pt.tecnico.bubbledocs.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pt.tecnico.bubbledocs.domain.Cell;
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
		GetSpreadSheetContent service = new GetSpreadSheetContent(spread.getID(), token);
        service.execute();
		
        Cell cellcontent1 = spread.getCell(5, 5);
        Cell cellcontent2 = spread.getCell(5, 6);
        Cell cellcontent3 = spread.getCell(15, 10);
        
        assertEquals(js.getUsername(), USERNAME);
        assertEquals(content, cellcontent1);
        assertEquals(content_1, cellcontent2);
        assertEquals(content_2, cellcontent3);
	}
	
	@Test(expected = SpreadsheetNotFoundException.class)
	public void invalidSpreadsheetIdentifier(){
		GetSpreadSheetContent service = new GetSpreadSheetContent(spread.getID()+10, token);
        service.execute();
	}
	
	@Test(expected = UnauthorizedOperationException.class)
	public void invalidUserTokenNotAllowed(){
		GetSpreadSheetContent service = new GetSpreadSheetContent(spread.getID(), USERNAME_DOES_NOT_EXIST);
        service.execute();
	}
	
	@Test(expected = SpreadsheetNotFoundException.class)
	public void invalidUserTokenNotInSession(){
		removeUserFromSession(js.getUsername());
    	GetSpreadSheetContent service = new GetSpreadSheetContent(spread.getID(), USERNAME);	
    	service.execute();
	}
	
	@Test(expected = SpreadsheetNotFoundException.class)
	public void invalidUserTokenDoesNotExist(){
    	GetSpreadSheetContent service = new GetSpreadSheetContent(spread.getID(), USERNAME);
    	service.execute();
    	assertEquals(js.getUsername(), "notExist");
	}
	
}
