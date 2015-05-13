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
import pt.tecnico.bubbledocs.exception.EmptyUsernameException;
import pt.tecnico.bubbledocs.exception.SpreadsheetNotFoundException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;

public class GetSpreadSheetContentTest extends BubbleDocsServiceTest{

    private static final String SPREADSHEET = "ODD";
    private static final String CREATOR = "Shkey";
    private static final String CREATOREMAIL = "sherl0ck@shneep";
    private static final String WRITER = "Sheepno";
    private static final String WRITEREMAIL = "dibidibidis@shneep";
    private static final String READER = "Oneepken";
    private static final String READEREMAIL = "0chicken@shneep";
    private static final String NOTALLOWED = "jongshp";
    private static final String NOTALLOWEDEMAIL = "trololol@shneep";
    private static final String USERNAME_DOES_NOT_EXIST = "Dorito";
    
    private static final Integer ROWS = 3;
    private static final Integer COLS = 3;
    
    private int ssId;
    private String creatorToken, writerToken, readerToken, notAllowedToken;
    private String[][] matrix = new String[ROWS][COLS];

	
	@Override
    public void populate4Test() throws BubbleDocsException {
		User creator = createUser(CREATOR, CREATOREMAIL, "Kim Kibum");
        User writer = createUser(WRITER, WRITEREMAIL, "Choi Minho");
        User reader = createUser(READER, READEREMAIL, "Lee Jinki");
        createUser(NOTALLOWED, NOTALLOWEDEMAIL, "Kim Jonghyun");

        creatorToken = addUserToSession(CREATOR);
        writerToken = addUserToSession(WRITER);
        readerToken = addUserToSession(READER);
        notAllowedToken = addUserToSession(NOTALLOWED);

        Spreadsheet ss = createSpreadSheet(creator, SPREADSHEET, ROWS, COLS);
        ssId = ss.getID();
        ss.addWriters(writer);
        ss.addReaders(reader);
       
        Cell one = ss.getCell(2, 2);
        Cell two = ss.getCell(2, 3);
        Cell three = ss.getCell(3, 1);
        
        one.setContent(new Literal(1));
        two.setContent(new Literal(2));
        three.setContent(new Literal(3));
        
        for(int i = 0; i < ROWS; i++){
			for(int j = 0; j < COLS; j++){
				matrix[i][j] = "";
			}
		}
		matrix[1][1] = "1";
		matrix[1][2] = "2";
		matrix[2][0] = "3";
        
    }
	
	@Test
	public void successCreator() throws BubbleDocsException {
		GetSpreadSheetContent service = new GetSpreadSheetContent(ssId, creatorToken);
        service.execute();
        String[][] result = service.getResult();
        
        for(int i = 0; i < ROWS; i++) {
        	for(int j = 0; j < COLS; j++) {
            	assertEquals(matrix[i][j], result[i][j]);
            }
        }
	}
	
	@Test
	public void successReader() throws BubbleDocsException {
		GetSpreadSheetContent service = new GetSpreadSheetContent(ssId, readerToken);
        service.execute();
        String[][] result = service.getResult();
        
        for(int i = 0; i < ROWS; i++) {
        	for(int j = 0; j < COLS; j++) {
            	assertEquals(matrix[i][j], result[i][j]);
            }
        }
	}
	
	@Test
	public void successWriter() throws BubbleDocsException {
		GetSpreadSheetContent service = new GetSpreadSheetContent(ssId, writerToken);
        service.execute();
        String[][] result = service.getResult();
        
        for(int i = 0; i < ROWS; i++) {
        	for(int j = 0; j < COLS; j++) {
            	assertEquals(matrix[i][j], result[i][j]);
            }
        }
	}
	
	@Test(expected = UserNotInSessionException.class)
	public void userNotInSession() throws BubbleDocsException {
		removeUserFromSession(creatorToken);
		GetSpreadSheetContent service = new GetSpreadSheetContent(ssId, creatorToken);
        service.execute();
        
	}
	
	@Test(expected = UserNotInSessionException.class)
	public void userDoesNotExist() throws BubbleDocsException {
		GetSpreadSheetContent service = new GetSpreadSheetContent(ssId, "ola88");
        service.execute();
      
	}
	
	@Test(expected = UnauthorizedOperationException.class)
	public void userNotAllowed() throws BubbleDocsException {
		GetSpreadSheetContent service = new GetSpreadSheetContent(ssId, notAllowedToken);
        service.execute();
        
	}
	
	@Test(expected = EmptyUsernameException.class)
	public void emptyUser() throws BubbleDocsException {
		GetSpreadSheetContent service = new GetSpreadSheetContent(ssId, "");
        service.execute();
        
	}
	
	@Test(expected = EmptyUsernameException.class)
	public void nullUser() throws BubbleDocsException {
		GetSpreadSheetContent service = new GetSpreadSheetContent(ssId, null);
        service.execute();
        
	}
	
	@Test(expected = SpreadsheetNotFoundException.class)
	public void spreadsheetDoesNotExist() throws BubbleDocsException {
		GetSpreadSheetContent service = new GetSpreadSheetContent(88, creatorToken);
        service.execute();
        
	}
	
}