package pt.tecnico.bubbledocs.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pt.tecnico.bubbledocs.domain.Cell;
import pt.tecnico.bubbledocs.domain.Content;
import pt.tecnico.bubbledocs.domain.Literal;
import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.CellOutOfBoundsException;
import pt.tecnico.bubbledocs.exception.SpreadsheetNotFoundException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;

public class AssignLiteralCellTest extends BubbleDocsServiceTest {

    private static final String SPREADSHEET = "Spreadsheep";
    private static final String CREATOR = "Shkey";
    private static final String CREATOREMAIL = "sherl0ck@shneep";
    private static final String WRITER = "Sheepno";
    private static final String WRITEREMAIL = "dibidibidis@shneep";
    private static final String READER = "Oneepken";
    private static final String READEREMAIL = "0chicken@shneep";
    private static final String NOTALLOWED = "jongshp";
    private static final String NOTALLOWEDEMAIL = "trololol@shneep";

    private static final String PROTECTED = "2;2";
    private static final String EMPTY = "3;3";
    private static final String FULL = "4;2";
    private static final String OUTX = "6;1";
    private static final String OUTY = "1;6";

    private int ssId;
    private String creatorToken, writerToken, readerToken, notAllowedToken;

    @Override
    public void populate4Test() throws BubbleDocsException {
        Content content = new Literal(100);
        User creator = createUser(CREATOR, CREATOREMAIL, "Kim Kibum");
        User writer = createUser(WRITER, WRITEREMAIL, "Choi Minho");
        User reader = createUser(READER, READEREMAIL, "Lee Jinki");
        createUser(NOTALLOWED, NOTALLOWEDEMAIL, "Kim Jonghyun");

        creatorToken = addUserToSession(CREATOR);
        writerToken = addUserToSession(WRITER);
        readerToken = addUserToSession(READER);
        notAllowedToken = addUserToSession(NOTALLOWED);

        Spreadsheet ss = createSpreadSheet(creator, SPREADSHEET, 5, 4);
        ssId = ss.getID();
        ss.addWriters(writer);
        ss.addReaders(reader);
        Cell protect = ss.getCell(2, 2);
        Cell full = ss.getCell(4, 2);

        protect.setProtect(true);
        full.setContent(content);
    }

    @Test
    public void success() throws BubbleDocsException {
        Integer test1 = 50;
        Integer test2 = 25;

        AssignLiteralCell service1 = new AssignLiteralCell(creatorToken, ssId, FULL, "50");
        service1.execute();
        assertEquals(test1, getSpreadSheet(SPREADSHEET).getCell(4, 2).getValue());

        AssignLiteralCell service2 = new AssignLiteralCell(writerToken, ssId, EMPTY, "25");
        service2.execute();
        assertEquals(test2, getSpreadSheet(SPREADSHEET).getCell(3, 3).getValue());
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void creatorAccessToProtectedCell() throws BubbleDocsException {
        AssignLiteralCell service = new AssignLiteralCell(creatorToken, ssId, PROTECTED, "91");
        service.execute();
    }

    @Test(expected = CellOutOfBoundsException.class)
    public void XCoordinateOutOfBounds() throws BubbleDocsException {
        AssignLiteralCell service = new AssignLiteralCell(creatorToken, ssId, OUTX, "88");
        service.execute();
    }

    @Test(expected = CellOutOfBoundsException.class)
    public void YCoordinateOutOfBounds() throws BubbleDocsException {
        AssignLiteralCell service = new AssignLiteralCell(creatorToken, ssId, OUTY, "88");
        service.execute();
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void readerWritingCell() throws BubbleDocsException {
        AssignLiteralCell service = new AssignLiteralCell(readerToken, ssId, EMPTY, "88");
        service.execute();
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void notAllowedWritingCell() throws BubbleDocsException {
        AssignLiteralCell service = new AssignLiteralCell(notAllowedToken, ssId, EMPTY, "88");
        service.execute();
    }

    @Test(expected = SpreadsheetNotFoundException.class)
    public void spreadSheetNotExist() throws BubbleDocsException {
        AssignLiteralCell service = new AssignLiteralCell(writerToken, 100, EMPTY, "88");
        service.execute();
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void invalidValue() throws BubbleDocsException {
        AssignLiteralCell service = new AssignLiteralCell(writerToken, ssId, EMPTY, "ola");
        service.execute();
    }

    @Test(expected = UserNotInSessionException.class)
    public void accessUsernameNotExist() {
        removeUserFromSession(creatorToken);
        AssignLiteralCell service = new AssignLiteralCell(creatorToken, ssId, EMPTY, "88");
        service.execute();
    }
}
