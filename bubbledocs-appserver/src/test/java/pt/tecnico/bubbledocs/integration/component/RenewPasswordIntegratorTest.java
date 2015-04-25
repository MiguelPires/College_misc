package pt.tecnico.bubbledocs.integration.component;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.naming.ServiceUnavailableException;

import mockit.Mock;
import mockit.MockUp;

import org.junit.Test;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.EmptyUsernameException;
import pt.tecnico.bubbledocs.exception.InvalidUsernameException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;
import pt.tecnico.bubbledocs.service.BubbleDocsServiceTest;
import pt.tecnico.bubbledocs.service.RenewPassword;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;

public class RenewPasswordIntegratorTest extends BubbleDocsServiceTest {
    private String ars;

    private static final String USERNAME = "ars";
    private static final String EMAIL = "ars@tecnico.pt";
    private static final String NAME = "António Rito Silva";

    @Override
    public void populate4Test() throws BubbleDocsException {
        createUser(USERNAME, EMAIL, NAME);
        ars = addUserToSession(USERNAME);
    }

    @Test
    public void success() {
        RenewPassword service = new RenewPassword(ars);
        service.execute();

        User user = getUserFromUsername(USERNAME);
        boolean loggedOut = getUserFromSession(ars) == null;

        assertFalse("Password renewed: no local copy.", user.getValidPassword());
        assertTrue("User is logged out", loggedOut);
    }

    @Test(expected = UnavailableServiceException.class)
    public void remoteServiceUnavailable() {

        new MockUp<IDRemoteServices>() {
            @Mock
            public void renewPassword(String username) throws ServiceUnavailableException {
                throw new RemoteInvocationException();
            }
        };
        RenewPassword service = new RenewPassword(ars);
        service.execute();
    }

    @Test(expected = UserNotInSessionException.class)
    public void unauthorizedRenewalUserNotInSession() {
        removeUserFromSession(ars);
        RenewPassword service = new RenewPassword(ars);
        service.execute();
    }

    @Test(expected = UserNotInSessionException.class)
    public void unauthorizedRenewalUserDoesNotExist() {
        RenewPassword service = new RenewPassword("ola9");
        service.execute();
    }

    @Test(expected = EmptyUsernameException.class)
    public void nullUserToken() {
        RenewPassword service = new RenewPassword(null);
        service.execute();
    }

    @Test(expected = EmptyUsernameException.class)
    public void emptyUserToken() {
        RenewPassword service = new RenewPassword("");
        service.execute();
    }
    
    @Test(expected = InvalidUsernameException.class)
    public void smallUserToken() {
        RenewPassword service = new RenewPassword("ab");
        service.execute();
    }
    
    @Test(expected = InvalidUsernameException.class)
    public void largeUserToken() {
        RenewPassword service = new RenewPassword("abwdqwwdwww9");
        service.execute();
    }
}
