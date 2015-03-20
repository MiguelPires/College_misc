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
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;

public class AssignReferenceCellTest extends BubbleDocsServiceTest {

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

    private static final String REFERENCE = "1;1";

    private int ssId;
    private String creatorToken, writerToken, readerToken, notAllowedToken;

    @Override
    public void populate4Test() throws BubbleDocsException {
        Content content = new Literal(100);
        Content content2 = new Literal(19);
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
        Cell ref = ss.getCell(1, 1);

        protect.setProtect(true);
        full.setContent(content);
        ref.setContent(content2);
    }

    @Test
    public void success() throws BubbleDocsException {

        AssignReferenceCell service1 = new AssignReferenceCell(creatorToken, ssId, FULL, REFERENCE);
        service1.execute();

        AssignReferenceCell service2 = new AssignReferenceCell(writerToken, ssId, EMPTY, REFERENCE);
        service2.execute();

        AssignReferenceCell service3 = new AssignReferenceCell(writerToken, ssId, EMPTY, PROTECTED);
        service3.execute();

        assertEquals("19", service1.getResult());
        assertEquals("19", service2.getResult());
        assertNull("Cell has content.", service2.getResult());
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void creatorAccessToProtectedCell() throws BubbleDocsException {
        AssignReferenceCell service = new AssignReferenceCell(creatorToken, ssId, PROTECTED,
                REFERENCE);
        service.execute();
    }

    @Test(expected = CellOutOfBoundsException.class)
    public void accessToNonExistentCell() throws BubbleDocsException {
        AssignReferenceCell service = new AssignReferenceCell(creatorToken, ssId, OUT, REFERENCE);
        service.execute();
    }

    @Test(expected = CellOutOfBoundsException.class)
    public void referenceToNonExistentCell() throws BubbleDocsException {
        AssignReferenceCell service = new AssignReferenceCell(creatorToken, ssId, EMPTY, OUT);
        service.execute();
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void readerWritingCell() throws BubbleDocsException {
        AssignReferenceCell service = new AssignReferenceCell(readerToken, ssId, EMPTY, REFERENCE);
        service.execute();
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void notAllowedWritingCell() throws BubbleDocsException {
        AssignReferenceCell service = new AssignReferenceCell(notAllowedToken, ssId, EMPTY,
                REFERENCE);
        service.execute();
    }
}
