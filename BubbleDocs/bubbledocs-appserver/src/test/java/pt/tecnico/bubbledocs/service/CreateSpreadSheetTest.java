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
    private static final String EMAIL = "vanessag@tecnico.pt";
    private static final String SPNAME = "Notas LEIC";
    private final Integer ROWS = 20;
    private final Integer COL = 10;

    @Override
    public void populate4Test() throws BubbleDocsException {
        createUser(USERNAME, EMAIL, "Vanessa Gaspar");
        vany = addUserToSession(USERNAME);
    }

    @Test
    public void success() {
        final CreateSpreadSheet service = new CreateSpreadSheet(vany, SPNAME, ROWS, COL);
        service.execute();
        final Spreadsheet createdSpreadsheet = getSpreadSheet(SPNAME);

        assertEquals(SPNAME, createdSpreadsheet.getName());
        assertEquals(ROWS, createdSpreadsheet.getRows());
        assertEquals(COL, createdSpreadsheet.getColumns());
    }

    //Creation with empty name 
    @Test(expected = EmptySpreadSheetNameException.class)
    public void emptyName() {
        final CreateSpreadSheet service = new CreateSpreadSheet(vany, "", ROWS, COL);
        service.execute();
    }

    //rows e columns negativos
    @Test(expected = InvalidSpreadsheetDimensionsException.class)
    public void negativeDimensions() {
        final CreateSpreadSheet service = new CreateSpreadSheet(vany, "Negative", -5, 1);
        service.execute();
    }

    @Test(expected = UserNotInSessionException.class)
    public void accessUsernameNotExist() {
        removeUserFromSession(vany);
        final CreateSpreadSheet service = new CreateSpreadSheet(vany, SPNAME, ROWS, COL);
        service.execute();
    }
}
