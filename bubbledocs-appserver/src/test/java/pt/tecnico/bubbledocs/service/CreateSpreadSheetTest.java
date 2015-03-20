package pt.tecnico.bubbledocs.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.EmptySpreadSheetNameException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;
import pt.tecnico.bubbledocs.service.CreateSpreadSheet;


public class CreateSpreadSheetTest extends BubbleDocsServiceTest {

	private String vany;
	private String userTest;
    private User creator;
    private Spreadsheet sp;
    private int id;
    
	private static final String USERNAME = "vany";
	private static final String PASSWORD = "lala123";
    private static final String SPNAME = "Notas LEIC";
    private int ROWS = 20;
    private int COL = 10;

   
    @Override
    public void populate4Test() throws BubbleDocsException {
    	creator = createUser(USERNAME, PASSWORD, "Vanessa Gaspar");
    	vany = addUserToSession("vany");
    	sp = createSpreadSheet(creator, SPNAME, ROWS, COL);
    	id = sp.getID();
    }

   
    @Test
    public void success() 
    {
    	CreateSpreadSheet service = new CreateSpreadSheet(vany, SPNAME, ROWS, COL);
	    service.execute();
	    
	    assertEquals(SPNAME, service.getSheetName());
	    assertEquals(id, service.getSheetId());
	    assertEquals(ROWS, service.getSheetRows());
	    assertEquals(COL, service.getSheetCol());
	    
    } 
    
    
    //Creation with empty name 
    @Test(expected = EmptySpreadSheetNameException.class)
    public void emptyName() 
    {
    	CreateSpreadSheet service = new CreateSpreadSheet(vany, "", ROWS, COL);
	    service.execute();
    }
    
   
    //Creation without user in session 
    @Test(expected = UserNotInSessionException.class)
    public void unauthorizedSpreadSheetCreation() 
    {
    	CreateSpreadSheet service = new CreateSpreadSheet(userTest, SPNAME, ROWS, COL);
        service.execute();
    }

    
}
