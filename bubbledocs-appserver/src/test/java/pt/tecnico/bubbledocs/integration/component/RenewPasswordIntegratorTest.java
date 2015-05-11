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
import pt.tecnico.bubbledocs.service.integration.RenewPasswordIntegrator;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;

public class RenewPasswordIntegratorTest extends BubbleDocsServiceTest {
    private String alice;

    private static final String USERNAME = "alice";
    private static final String EMAIL = "alice@tecnico.pt";
    private static final String NAME = "Alice Sheepires";

    @Override
    public void populate4Test() throws BubbleDocsException {
        createUser(USERNAME, EMAIL, NAME);
        alice = addUserToSession(USERNAME);
    }

    @Test
    public void success() {
        RenewPasswordIntegrator service = new RenewPasswordIntegrator(alice);
        service.execute();

        User user = getUserFromUsername(USERNAME);
        boolean loggedOut = getUserFromSession(alice) == null;

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
        RenewPasswordIntegrator service = new RenewPasswordIntegrator(alice);
        service.execute();
    }

    @Test(expected = UserNotInSessionException.class)
    public void unauthorizedRenewalUserNotInSession() {
        removeUserFromSession(alice);
        RenewPasswordIntegrator service = new RenewPasswordIntegrator(alice);
        service.execute();
    }

    @Test(expected = UserNotInSessionException.class)
    public void unauthorizedRenewalUserDoesNotExist() {
        RenewPasswordIntegrator service = new RenewPasswordIntegrator("ola9");
        service.execute();
    }

    @Test(expected = EmptyUsernameException.class)
    public void nullUserToken() {
        RenewPasswordIntegrator service = new RenewPasswordIntegrator(null);
        service.execute();
    }

    @Test(expected = EmptyUsernameException.class)
    public void emptyUserToken() {
        RenewPasswordIntegrator service = new RenewPasswordIntegrator("");
        service.execute();
    }

    @Test(expected = InvalidUsernameException.class)
    public void smallUserToken() {
        RenewPasswordIntegrator service = new RenewPasswordIntegrator("ab");
        service.execute();
    }

    @Test(expected = InvalidUsernameException.class)
    public void largeUserToken() {
        RenewPasswordIntegrator service = new RenewPasswordIntegrator("abwdqwwdwww9");
        service.execute();
    }
}
