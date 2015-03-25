package pt.tecnico.bubbledocs.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.EmptySpreadSheetNameException;
import pt.tecnico.bubbledocs.exception.InvalidSpreadsheetDimensionsException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;


public class CreateSpreadSheetTest extends BubbleDocsServiceTest {

    private String vany;
    private static final String USERNAME = "vany";
    private static final String PASSWORD = "lala123";
    private static final String SPNAME = "Notas LEIC";
    private Integer ROWS = 20;
    private Integer COL = 10;


    @Override
    public void populate4Test() throws BubbleDocsException {
        createUser(USERNAME, PASSWORD, "Vanessa Gaspar");
        vany = addUserToSession(USERNAME);

    }

    @Test
    public void success() {
        CreateSpreadSheet service = new CreateSpreadSheet(vany, SPNAME, ROWS, COL);
        service.execute();
        Spreadsheet createdSpreadsheet = getSpreadSheet(SPNAME);

        assertEquals(SPNAME, createdSpreadsheet.getName());
        assertEquals(ROWS, createdSpreadsheet.getRows());
        assertEquals(COL, createdSpreadsheet.getColumns());
    }


    //Creation with empty name 
    @Test(expected = EmptySpreadSheetNameException.class)
    public void emptyName() {
        CreateSpreadSheet service = new CreateSpreadSheet(vany, "", ROWS, COL);
        service.execute();
    }

    //rows e columns negativos
    @Test(expected = InvalidSpreadsheetDimensionsException.class)
    public void negativeDimensions() {
        CreateSpreadSheet service = new CreateSpreadSheet(vany, "Negative", -5, 1);
        service.execute();
    }
    
    @Test(expected = UserNotInSessionException.class)
    public void accessUsernameNotExist() {
        removeUserFromSession(vany);
        CreateSpreadSheet service = new CreateSpreadSheet(vany, SPNAME, ROWS, COL);
        service.execute();
    }
}
