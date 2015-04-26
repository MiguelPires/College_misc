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

public class AssignBinaryFunctionToCellTest extends BubbleDocsServiceTest {

    private static final String SPREADSHEET = "Spreadsheep";
    private static final String CREATOR = "Shkey";
    private static final String CREATOREMAIL = "sherl0ck@shneep";
    private static final String WRITER = "Sheepno";
    private static final String WRITEREMAIL = "dibidibidis@shneep";
    private static final String READER = "Oneepken";
    private static final String READEREMAIL = "0chicken@shneep";
    private static final String NOTALLOWED = "jongshp";
    private static final String NOTALLOWEDEMAIL = "trololol@shneep";
    
    private static final String ADDITION = "ADD(2,2)";
    private static final String DIVISION = "DIV(2,2)";
    private static final String MULTIPLICATION = "MUL(2,2)";
    private static final String SUBTRACTION = "SUB(2,2)";

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
        AssignBinaryFunctionToCell addService = new AssignBinaryFunctionToCell(EMPTY, ADDITION, ssId, creatorToken);
        addService.execute();
        assertEquals(4, getSpreadSheet(SPREADSHEET).getCell(3, 3).getValue());
        
        AssignBinaryFunctionToCell divService = new AssignBinaryFunctionToCell(EMPTY, DIVISION, ssId, creatorToken);
        divService.execute();
        assertEquals(1, getSpreadSheet(SPREADSHEET).getCell(3, 3).getValue());
        
        AssignBinaryFunctionToCell mulService = new AssignBinaryFunctionToCell(EMPTY, MULTIPLICATION, ssId, creatorToken);
        mulService.execute();
        assertEquals(4, getSpreadSheet(SPREADSHEET).getCell(3, 3).getValue());
        
        AssignBinaryFunctionToCell subService = new AssignBinaryFunctionToCell(EMPTY, SUBTRACTION, ssId, creatorToken);
        subService.execute();
        assertEquals(0, getSpreadSheet(SPREADSHEET).getCell(3, 3).getValue());
        
        AssignBinaryFunctionToCell addServiceFull = new AssignBinaryFunctionToCell(FULL, ADDITION, ssId, creatorToken);
        addServiceFull.execute();
        assertEquals(4, getSpreadSheet(SPREADSHEET).getCell(3, 3).getValue());
        
        AssignBinaryFunctionToCell divServiceFull = new AssignBinaryFunctionToCell(FULL, DIVISION, ssId, creatorToken);
        divServiceFull.execute();
        assertEquals(1, getSpreadSheet(SPREADSHEET).getCell(3, 3).getValue());
        
        AssignBinaryFunctionToCell mulServiceFull = new AssignBinaryFunctionToCell(FULL, MULTIPLICATION, ssId, creatorToken);
        mulServiceFull.execute();
        assertEquals(4, getSpreadSheet(SPREADSHEET).getCell(3, 3).getValue());
        
        AssignBinaryFunctionToCell subServiceFull = new AssignBinaryFunctionToCell(FULL, SUBTRACTION, ssId, creatorToken);
        subServiceFull.execute();
        assertEquals(0, getSpreadSheet(SPREADSHEET).getCell(3, 3).getValue());
    }

    @Test(expected = CellOutOfBoundsException.class)
    public void xCoordinateOutOfBounds() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(OUTX, ADDITION, ssId, creatorToken);
        service.execute();
    }

    @Test(expected = CellOutOfBoundsException.class)
    public void yCoordinateOutOfBounds() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(OUTY, ADDITION, ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void creatorAccessToProtectedCell() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(PROTECTED, ADDITION, ssId, creatorToken);
        service.execute();
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void additionTooFewArguments() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "ADD(1)", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void additionTooManyArguments() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "ADD(1,2,2;2)", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void additionEmptyArgument() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "ADD()", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void additionInvalidReferenceCell() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "ADD(1,6;6)", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void additionInvalidArguments() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "ADD(ola,2)", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void divisionTooFewArguments() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "DIV(1)", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void divisionTooManyArguments() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "DIV(1,2,2;2)", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void divisionEmptyArgument() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "DIV()", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void divisionInvalidReferenceCell() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "DIV(1,6;6)", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void divisionInvalidArguments() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "DIV(ola,2)", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void multiplicationTooFewArguments() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "MUL(1)", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void multiplicationTooManyArguments() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "MUL(1,2,2;2)", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void multiplicationEmptyArgument() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "MUL()", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void multiplicationInvalidReferenceCell() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "MUL(1,6;6)", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void multiplicationInvalidArguments() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "MUL(ola,2)", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void subtractionTooFewArguments() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "SUB(1)", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void subtractionTooManyArguments() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "SUB(1,2,2;2)", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void subtractionEmptyArgument() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "SUB()", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void subtractionInvalidReferenceCell() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "SUB(1,6;6)", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void subtractionInvalidArguments() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "SUB(ola,2)", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void invalidFunction() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, "OLA(1,2)", ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = SpreadsheetNotFoundException.class)
    public void spreadSheetNotExist() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, ADDITION, "88", creatorToken);
        service.execute();
    }
    
    @Test(expected = UnauthorizedOperationException.class)
    public void readerWritingCell() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, ADDITION, ssId, readerToken);
        service.execute();
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void notAllowedWritingCell() throws BubbleDocsException {
    	AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, ADDITION, ssId, notAllowedToken);
        service.execute();
    }
    @Test(expected = UserNotInSessionException.class)
    public void userNotInSession() {
        removeUserFromSession(creatorToken);
        AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, ADDITION, ssId, creatorToken);
        service.execute();
    }
    
    @Test(expected = UserNotInSessionException.class)
    public void userDoesNotExist() {
        AssignBinaryFunctionToCell service = new AssignBinaryFunctionToCell(EMPTY, ADDITION, ssId, "kai8");
        service.execute();
    }
}
