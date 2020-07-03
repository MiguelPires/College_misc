package pt.tecnico.bubbledocs.integration.component;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import mockit.Mock;
import mockit.MockUp;

import org.junit.Test;

import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.exception.UnknownBubbleDocsUserException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;
import pt.tecnico.bubbledocs.service.BubbleDocsServiceTest;
import pt.tecnico.bubbledocs.service.DeleteUser;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;

// add needed import declarations

public class DeleteUserIntegratorTest extends BubbleDocsServiceTest {

    private static final String USERNAME_TO_DELETE = "bruno";
    private static final String USERNAME = "alice";
    private static final String EMAIL = "alice@tecnico.pt";
    private static final String ROOT_USERNAME = "root";
    private static final String USERNAME_DOES_NOT_EXIST = "no-one";
    private static final String SPREADSHEET_NAME = "spread";

    private String root;
    private int ssId;

    @Override
    public void populate4Test() {
        createUser(USERNAME, EMAIL, "Alice Sheepires");
        User bruno = createUser(USERNAME_TO_DELETE, "bruno@tecnico.pt", "Bruno Sheepires");
        Spreadsheet ss = createSpreadSheet(bruno, SPREADSHEET_NAME, 20, 20);

        ssId = ss.getID();
        root = addUserToSession(ROOT_USERNAME);
    };

    public void success() {
        DeleteUser service = new DeleteUser(root, USERNAME_TO_DELETE);
        service.execute();
        boolean deleted = getUserFromUsername(USERNAME_TO_DELETE) == null;

        assertTrue(deleted);
        assertNull(getSpreadSheet(SPREADSHEET_NAME));
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
    public void successToDeleteIsInSession() {
        String token = addUserToSession(USERNAME_TO_DELETE);
        success();
        assertNull(getUserFromSession(token));
    }

    @Test(expected = UnknownBubbleDocsUserException.class)
    public void userToDeleteDoesNotExist() {
        DeleteUser service = new DeleteUser(root, USERNAME_DOES_NOT_EXIST);
        service.execute();
    }

    @Test(expected = UserNotInSessionException.class)
    public void rootNotInSession() {
        removeUserFromSession(root);
        new DeleteUser(root, USERNAME_TO_DELETE).execute();
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void notRootUser() {
        String alice = addUserToSession(USERNAME);
        new DeleteUser(alice, USERNAME_TO_DELETE).execute();
    }

    @Test(expected = UserNotInSessionException.class)
    public void notInSessionAndNotRoot() {
        String alice = addUserToSession(USERNAME);
        removeUserFromSession(alice);
        new DeleteUser(alice, USERNAME_TO_DELETE).execute();

    }

    //Remote service fails but user is not deleted locally    
    @Test
    public void idServiceUnavailableUserNotDeleted() {
        new MockUp<IDRemoteServices>() {
            @Mock
            public void removeUser(String username) {
                throw new RemoteInvocationException();
            }
        };
        try {
            DeleteUser service = new DeleteUser(root, USERNAME_TO_DELETE);
            service.execute();
        } catch (UnavailableServiceException e) {
            boolean deleted = getUserFromUsername(USERNAME_TO_DELETE) == null;

            assertFalse(deleted);
            assertNotNull(getSpreadSheet(SPREADSHEET_NAME));
        }
    }

}
