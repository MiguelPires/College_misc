package pt.tecnico.bubbledocs.service;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import org.junit.After;
import org.junit.Before;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.core.WriteOnReadError;
import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UnknownBubbleDocsUserException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;

// add needed import declarations

public class BubbleDocsServiceTest {

    @Before
    public void setUp() throws Exception {

        try {
            FenixFramework.getTransactionManager().begin(false);
            populate4Test();
        } catch (WriteOnReadError | NotSupportedException | SystemException e1) {
            e1.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        try {
            FenixFramework.getTransactionManager().rollback();
        } catch (IllegalStateException | SecurityException | SystemException e) {
            e.printStackTrace();
        }
    }

    // should redefine this method in the subclasses if it is needed to specify
    // some initial state
    public void populate4Test() throws BubbleDocsException {
    }

    // auxiliary methods that access the domain layer and are needed in the test classes
    // for defining the initial state and checking that the service has the expected behavior
    public User createUser(String username, String password, String name) throws BubbleDocsException {
        return BubbleDocsService.createUser(username, name, password);
    }

    public Spreadsheet createSpreadSheet(User user, String name, int row, int column) {
	
    	return BubbleDocsService.createSpreadSheet(user, name, row, column);
    }

    // returns a spreadsheet whose name is equal to name
    public Spreadsheet getSpreadSheet(String name) {
        return BubbleDocsService.getSpreadsheet(name);

    }

    // returns the user registered in the application whose username is equal to username
    public User getUserFromUsername(String username) {
    	try{
    		return BubbleDocsService.getUser(username);
    	}catch(UnknownBubbleDocsUserException e){
    		return null;
    	}
    }

    // put a user into session and returns the token associated to it
    public String addUserToSession(String username) throws UnknownBubbleDocsUserException {
    	return BubbleDocsService.addUserToSession(username);
    }

    // remove a user from session given its token
    public void removeUserFromSession(String token) throws UserNotInSessionException {
    	BubbleDocsService.removeUserByToken(token);

    }

    // return the user registered in session whose token is equal to token
    public User getUserFromSession(String token){
        try{
            return BubbleDocsService.getUserByToken(token);
        } catch (UserNotInSessionException e)
        {
            return null;
        }
    }

}
