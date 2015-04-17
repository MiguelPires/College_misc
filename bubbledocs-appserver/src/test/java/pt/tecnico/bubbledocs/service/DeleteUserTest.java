package pt.tecnico.bubbledocs.service;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import mockit.Mock;
import mockit.MockUp;

import org.junit.Test;

import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UnknownBubbleDocsUserException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;

// add needed import declarations

public class DeleteUserTest extends BubbleDocsServiceTest {

    private static final String USERNAME_TO_DELETE = "smf";
    private static final String USERNAME = "ars";
    private static final String EMAIL = "ars@tecnico.pt";
    private static final String ROOT_USERNAME = "root";
    private static final String USERNAME_DOES_NOT_EXIST = "no-one";
    private static final String SPREADSHEET_NAME = "spread";

    private String root;
    private int ssId;

    @Override
    public void populate4Test() {
        createUser(USERNAME, EMAIL, "António Rito Silva");
        User smf = createUser(USERNAME_TO_DELETE, "smf@tecnico.pt", "Sérgio Fernandes");
        Spreadsheet ss = createSpreadSheet(smf, SPREADSHEET_NAME, 20, 20);

        ssId = ss.getID();
        root = addUserToSession(ROOT_USERNAME);
    };

    public void success() {
        new MockUp<IDRemoteServices>() {
            @Mock
            public void removeUser(String username) throws RemoteInvocationException {
                ;
            }
        };

        DeleteUser service = new DeleteUser(root, USERNAME_TO_DELETE);
        service.execute();
        boolean deleted = getUserFromUsername(USERNAME_TO_DELETE) == null;

        assertTrue("user was not deleted", deleted);
        assertNull("Spreadsheet was not deleted", getSpreadSheet(SPREADSHEET_NAME));
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
        assertNull("Removed user but not removed from session", getUserFromSession(token));
    }

    @Test(expected = UnknownBubbleDocsUserException.class)
    public void userToDeleteDoesNotExist() {
        new MockUp<IDRemoteServices>() {
            @Mock
            public void removeUser(String username) throws RemoteInvocationException {
                throw new UnknownBubbleDocsUserException();
            }
        };

        new DeleteUser(root, USERNAME_DOES_NOT_EXIST).execute();
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void notRootUser() {
        new MockUp<IDRemoteServices>() {
            @Mock
            public void removeUser(String username) throws RemoteInvocationException {
                throw new UnauthorizedOperationException();
            }
        };

        String ars = addUserToSession(USERNAME);
        new DeleteUser(ars, USERNAME_TO_DELETE).execute();
    }

    @Test(expected = UserNotInSessionException.class)
    public void rootNotInSession() {
        new MockUp<IDRemoteServices>() {
            @Mock
            public void removeUser(String username) throws RemoteInvocationException {
                throw new UserNotInSessionException();
            }
        };

        removeUserFromSession(root);
        new DeleteUser(root, USERNAME_TO_DELETE).execute();
    }

    @Test(expected = UserNotInSessionException.class)
    public void notInSessionAndNotRoot() {
        new MockUp<IDRemoteServices>() {
            @Mock
            public void removeUser(String username) throws RemoteInvocationException {
                throw new UserNotInSessionException();
            }
        };

        String ars = addUserToSession(USERNAME);
        removeUserFromSession(ars);
        new DeleteUser(ars, USERNAME_TO_DELETE).execute();

    }

    @Test(expected = UserNotInSessionException.class)
    public void accessUserDoesNotExist() {
        new MockUp<IDRemoteServices>() {
            @Mock
            public void removeUser(String username) throws RemoteInvocationException {
                throw new UserNotInSessionException();
            }
        };

        new DeleteUser(USERNAME_DOES_NOT_EXIST, USERNAME_TO_DELETE).execute();
    }
}
