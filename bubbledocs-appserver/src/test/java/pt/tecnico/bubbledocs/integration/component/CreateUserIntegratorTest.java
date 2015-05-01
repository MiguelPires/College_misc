package pt.tecnico.bubbledocs.integration.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import mockit.Mock;
import mockit.MockUp;

import org.junit.Test;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.DuplicateEmailException;
import pt.tecnico.bubbledocs.exception.DuplicateUsernameException;
import pt.tecnico.bubbledocs.exception.EmptyUsernameException;
import pt.tecnico.bubbledocs.exception.InvalidEmailException;
import pt.tecnico.bubbledocs.exception.InvalidUsernameException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;
import pt.tecnico.bubbledocs.service.BubbleDocsServiceTest;
import pt.tecnico.bubbledocs.service.integration.CreateUserIntegrator;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;



public class CreateUserIntegratorTest extends BubbleDocsServiceTest {

    private String root;
    private String ars;

    private static final String USERNAME = "ars";
    private static final String EMAIL = "ars@tecnico.pt";
    private static final String ROOT_USERNAME = "root";
    private static final String USERNAME_DOES_NOT_EXIST = "no-one";

    @Override
    public void populate4Test() throws BubbleDocsException {
        createUser(USERNAME, EMAIL, "António Rito Silva");
        root = addUserToSession(ROOT_USERNAME);
        ars = addUserToSession("ars");
    }

    @Test
    public void success() {
        CreateUserIntegrator service = new CreateUserIntegrator(root, USERNAME_DOES_NOT_EXIST, "jose@tecnico.pt",
            "José Ferreira");
        service.execute();

        User user = getUserFromUsername(USERNAME_DOES_NOT_EXIST);

        assertEquals(USERNAME_DOES_NOT_EXIST, user.getUsername());
        assertEquals("jose@tecnico.pt", user.getEmail());
        assertEquals("José Ferreira", user.getName());
    }

    @Test(expected = DuplicateUsernameException.class)
    public void usernameExists() {
        new MockUp<IDRemoteServices>() {
            @Mock
            public void createUser(String username, String email) {
                throw new DuplicateUsernameException();
            }
        };

        CreateUserIntegrator service = new CreateUserIntegrator(root, USERNAME, "jose@tecnico.pt", "José Ferreira");
        service.execute();
    }

    @Test(expected = EmptyUsernameException.class)
    public void emptyUsername() {
        new MockUp<IDRemoteServices>() {
            @Mock
            public void createUser(String username, String email) {
                throw new EmptyUsernameException();
            }
        };

        CreateUserIntegrator service = new CreateUserIntegrator(root, "", "jose@tecnico.pt", "José Ferreira");
        service.execute();
    }

    @Test(expected = InvalidUsernameException.class)
    public void invalidUsernameTooShort() {
        new MockUp<IDRemoteServices>() {
            @Mock
            public void createUser(String username, String email) {
                throw new InvalidUsernameException();
            }
        };

        CreateUserIntegrator service = new CreateUserIntegrator(root, "jo", "jose@tecnico.pt", "José Ferreira");
        service.execute();
    }

    @Test(expected = InvalidUsernameException.class)
    public void invalidUsernameTooLong() {
        new MockUp<IDRemoteServices>() {
            @Mock
            public void createUser(String username, String email) {
                throw new InvalidUsernameException();
            }
        };

        CreateUserIntegrator service = new CreateUserIntegrator(root, "josejosejose", "jose@tecnico.pt",
                "José Ferreira");
        service.execute();
    }

    @Test(expected = InvalidEmailException.class)
    public void invalidEmail() {
        new MockUp<IDRemoteServices>() {
            @Mock
            public void createUser(String username, String email) {
                throw new InvalidEmailException();
            }
        };

        CreateUserIntegrator service = new CreateUserIntegrator(root, USERNAME_DOES_NOT_EXIST, "@tecnico.pt",
                "José Ferreira");
        service.execute();
    }

    @Test(expected = UnauthorizedOperationException.class)
    public void unauthorizedUserCreation() {
        CreateUserIntegrator service = new CreateUserIntegrator(ars, USERNAME_DOES_NOT_EXIST, "jose@tecnico.pt",
                "José Ferreira");
        service.execute();
    }

    @Test(expected = UserNotInSessionException.class)
    public void unauthorizedUserCreationNotInSession() {
        removeUserFromSession(ars);
        CreateUserIntegrator service = new CreateUserIntegrator(ars, USERNAME_DOES_NOT_EXIST, "jose@tecnico.pt",
                "José Ferreira");
        service.execute();
    }

    @Test(expected = UserNotInSessionException.class)
    public void accessUsernameNotExist() {
        removeUserFromSession(root);
        CreateUserIntegrator service = new CreateUserIntegrator(root, USERNAME_DOES_NOT_EXIST, "jose@tecnico.pt",
                "José Ferreira");
        service.execute();
    }

    @Test(expected = UserNotInSessionException.class)
    public void unauthorizedUserCreationDoesNotExist() {
        CreateUserIntegrator service = new CreateUserIntegrator("ola2", USERNAME_DOES_NOT_EXIST, "jose@tecnico.pt",
                "José Ferreira");
        service.execute();
    }

    @Test
    public void remoteServiceFails() {
        CreateUserIntegrator service = new CreateUserIntegrator(root, USERNAME_DOES_NOT_EXIST, "jose@tecnico.pt",
                "José Ferreira");
        
        new MockUp<IDRemoteServices>() {
            @Mock
            public void createUser(String username, String email) throws InvalidUsernameException,
                                                         DuplicateUsernameException,
                                                         DuplicateEmailException,
                                                         InvalidEmailException,
                                                         RemoteInvocationException {

                throw new RemoteInvocationException();
            }
        };
        
        try {
            service.execute();

        } catch (UnavailableServiceException e){
            boolean wasNotCreated = getUserFromUsername(USERNAME_DOES_NOT_EXIST) == null;
            assertTrue(wasNotCreated);
        }
        
        
    }
}
