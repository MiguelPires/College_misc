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
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;

public class AssignLiteralCellTest extends BubbleDocsServiceTest {

    private static final String SPREADSHEET = "Spreadsheep";
    private static final String CREATOR = "Shkey";
    private static final String CREATORPASSWORD = "sherl0ck";
    private static final String WRITER = "Sheepno";
    private static final String WRITERPASSWORD = "dibidibidis";
    private static final String READER = "Oneepken";
    private static final String READERPASSWORD = "0Chicken";
    private static final String NOTALLOWED = "Jongsheep";
    private static final String NOTALLOWEDPASSWORD = "Trololol";

    private static final String PROTECTED = "2;2";
    private static final String EMPTY = "3;3";
    private static final String FULL = "4;2";
    private static final String OUT = "6;6";

    private int ssId;
    private String creatorToken, writerToken, readerToken, notAllowedToken;

    @Override
    public void populate4Test() throws BubbleDocsException {
        Content content = new Literal(100);
        User creator = createUser(CREATOR, CREATORPASSWORD, "Kim Kibum");
        User writer = createUser(WRITER, WRITERPASSWORD, "Choi Minho");
        User reader = createUser(READER, READERPASSWORD, "Lee Jinki");
        createUser(NOTALLOWED, NOTALLOWEDPASSWORD, "Kim Jonghyun");

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

        AssignLiteralCell service1 = new AssignLiteralCell(creatorToken, ssId, FULL, "50");
        service1.execute();

        AssignLiteralCell service2 = new AssignLiteralCell(writerToken, ssId, EMPTY, "25");
        service2.execute();

        assertEquals("50", service1.getResult());
        assertEquals("25", service2.getResult());
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void creatorAccessToProtectedCell() throws BubbleDocsException {
        AssignLiteralCell service = new AssignLiteralCell(creatorToken, ssId, PROTECTED, "91");
        service.execute();
    }

    @Test(expected = CellOutOfBoundsException.class)
    public void accessToNonExistentCell() throws BubbleDocsException {
        AssignLiteralCell service = new AssignLiteralCell(creatorToken, ssId, OUT, "88");
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
}
