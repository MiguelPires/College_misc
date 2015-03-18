package pt.tecnico.bubbledocs.service;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UnknownBubbleDocsUserException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;

// add needed import declarations

public class DeleteUserTest extends BubbleDocsServiceTest {

    private static final String USERNAME_TO_DELETE = "smf";
    private static final String USERNAME = "ars";
    private static final String PASSWORD = "ars";
    private static final String ROOT_USERNAME = "root";
    private static final String USERNAME_DOES_NOT_EXIST = "no-one";
    private static final String SPREADSHEET_NAME = "spread";

    // the tokens for user root
    private String root;

    @Override
    public void populate4Test() throws UnknownBubbleDocsUserException {
        createUser(USERNAME, PASSWORD, "António Rito Silva");
        User smf = createUser(USERNAME_TO_DELETE, "smf", "Sérgio Fernandes");
        createSpreadSheet(smf, USERNAME_TO_DELETE, 20, 20);

        root = addUserToSession(ROOT_USERNAME);
    };

    public void success() {
       
        try {
            DeleteUser service = new DeleteUser(root, USERNAME_TO_DELETE);
            service.execute();
            boolean deleted = getUserFromUsername(USERNAME_TO_DELETE) == null;

            assertTrue("user was not deleted", deleted);

            assertNull("Spreadsheet was not deleted",
                    getSpreadSheet(SPREADSHEET_NAME));
        } catch (BubbleDocsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * accessUsername exists, is in session and is root toDeleteUsername exists
     * and is not in session
     */
    @Test
    public void successToDeleteIsNotInSession() {
        success();
    }

    /*
     * accessUsername exists, is in session and is root toDeleteUsername exists
     * and is in session Test if user and session are both deleted
     */
    @Test
    public void successToDeleteIsInSession() throws BubbleDocsException {
        String token = addUserToSession(USERNAME_TO_DELETE);
        success();
        assertNull("Removed user but not removed from session", getUserFromSession(token));
    }

    @Test(expected = UnknownBubbleDocsUserException.class)
    public void userToDeleteDoesNotExist() {
        try {
            new DeleteUser(root, USERNAME_DOES_NOT_EXIST).execute();
        } catch (BubbleDocsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void notRootUser() {
        try {
            String ars = addUserToSession(USERNAME);

            new DeleteUser(ars, USERNAME_TO_DELETE).execute();
        } catch (BubbleDocsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test(expected = UserNotInSessionException.class)
    public void rootNotInSession() {

        try {
            removeUserFromSession(root);

            new DeleteUser(root, USERNAME_TO_DELETE).execute();
        } catch (BubbleDocsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test(expected = UserNotInSessionException.class)
    public void notInSessionAndNotRoot() {
        try {
            String ars = addUserToSession(USERNAME);
            removeUserFromSession(ars);
            new DeleteUser(ars, USERNAME_TO_DELETE).execute();
        } catch (BubbleDocsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test(expected = UserNotInSessionException.class)
    public void accessUserDoesNotExist() {
        try {
            new DeleteUser(USERNAME_DOES_NOT_EXIST, USERNAME_TO_DELETE).execute();
        } catch (BubbleDocsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
