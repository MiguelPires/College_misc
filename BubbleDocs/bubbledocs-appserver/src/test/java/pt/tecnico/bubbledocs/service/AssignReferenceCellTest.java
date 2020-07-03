package pt.tecnico.bubbledocs.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

public class AssignReferenceCellTest extends BubbleDocsServiceTest {

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
    private static final String REFERENCE = "1;1";

    private int ssId;
    private String creatorToken, writerToken, readerToken, notAllowedToken;

    @Override
    public void populate4Test() throws BubbleDocsException {
        Content content = new Literal(100);
        Content content2 = new Literal(19);
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
        Cell ref = ss.getCell(1, 1);

        protect.setProtect(true);
        full.setContent(content);
        ref.setContent(content2);
    }

    @Test
    public void success() throws BubbleDocsException {
        Integer test1 = 19;

        AssignReferenceCell service1 = new AssignReferenceCell(creatorToken, ssId, FULL, REFERENCE);
        service1.execute();
        assertEquals(test1, getSpreadSheet(SPREADSHEET).getCell(4, 2).getValue());

        AssignReferenceCell service2 = new AssignReferenceCell(writerToken, ssId, EMPTY, REFERENCE);
        service2.execute();
        assertEquals(test1, getSpreadSheet(SPREADSHEET).getCell(3, 3).getValue());

        AssignReferenceCell service3 = new AssignReferenceCell(writerToken, ssId, EMPTY, PROTECTED);
        service3.execute();
        assertNull("Cell has content.", getSpreadSheet(SPREADSHEET).getCell(3, 3).getValue());
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void creatorAccessToProtectedCell() {
        AssignReferenceCell service = new AssignReferenceCell(creatorToken, ssId, PROTECTED,
                REFERENCE);
        service.execute();
    }

    @Test(expected = CellOutOfBoundsException.class)
    public void XCoordinateOutOfBounds() {
        AssignReferenceCell service = new AssignReferenceCell(creatorToken, ssId, OUTX, REFERENCE);
        service.execute();
    }

    @Test(expected = CellOutOfBoundsException.class)
    public void YCoordinateOutOfBounds() {
        AssignReferenceCell service = new AssignReferenceCell(creatorToken, ssId, OUTY, REFERENCE);
        service.execute();
    }

    @Test(expected = CellOutOfBoundsException.class)
    public void XCoordinateOfReferenceOutOfBounds() {
        AssignReferenceCell service = new AssignReferenceCell(creatorToken, ssId, EMPTY, OUTX);
        service.execute();
    }

    @Test(expected = CellOutOfBoundsException.class)
    public void YCoordinateOfReferenceOutOfBounds() {
        AssignReferenceCell service = new AssignReferenceCell(creatorToken, ssId, EMPTY, OUTY);
        service.execute();
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void readerWritingCell() {
        AssignReferenceCell service = new AssignReferenceCell(readerToken, ssId, EMPTY, REFERENCE);
        service.execute();
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void notAllowedWritingCell() {
        AssignReferenceCell service = new AssignReferenceCell(notAllowedToken, ssId, EMPTY,
                REFERENCE);
        service.execute();
    }

    @Test(expected = SpreadsheetNotFoundException.class)
    public void spreadSheetNotExist() throws BubbleDocsException {
        AssignReferenceCell service = new AssignReferenceCell(writerToken, 100, EMPTY, REFERENCE);
        service.execute();
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void invalidValue() throws BubbleDocsException {
        AssignReferenceCell service = new AssignReferenceCell(writerToken, ssId, EMPTY, "ola;adeus");
        service.execute();
    }

    @Test(expected = UserNotInSessionException.class)
    public void accessUsernameNotExist() {
        removeUserFromSession(creatorToken);
        AssignReferenceCell service = new AssignReferenceCell(creatorToken, ssId, EMPTY, REFERENCE);
        service.execute();
    }
}
