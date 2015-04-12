package pt.tecnico.bubbledocs.service;

import static org.junit.Assert.assertEquals;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

import org.junit.Test;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.DuplicateUsernameException;
import pt.tecnico.bubbledocs.exception.EmptyUsernameException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;

public class CreateUserTest extends BubbleDocsServiceTest {

    private String root;
    private String ars;

    private static final String USERNAME = "ars";
    private static final String EMAIL = "ars@tecnico.pt";
    private static final String ROOT_USERNAME = "root";
    private static final String USERNAME_DOES_NOT_EXIST = "no-one";
    
    private IDRemoteServices serviceId;

    @Override
    public void populate4Test() throws BubbleDocsException {
        createUser(USERNAME, EMAIL, "António Rito Silva");
        root = addUserToSession(ROOT_USERNAME);
        ars = addUserToSession("ars");
    }

    @Test
    public void success() {
    	new MockUp<IDRemoteServices>() {
  		   @Mock
  		   public void createUser(String username, String email) {
  			   //
  		   }
        };
    	
    	CreateUser service = new CreateUser(root, USERNAME_DOES_NOT_EXIST, "jose@tecnico.pt", "José Ferreira");
        
        service.execute();
        
        User user = getUserFromUsername(USERNAME_DOES_NOT_EXIST);

        assertEquals(USERNAME_DOES_NOT_EXIST, user.getUsername());
        assertEquals("jose", user.getPassword());
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
    	
    	CreateUser service = new CreateUser(root, USERNAME, "jose@tecnico.pt", "José Ferreira");
    	       
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
    	
    	CreateUser service = new CreateUser(root, "", "jose@tecnico.pt", "José Ferreira");
        
        service.execute();

    }

    @Test(expected = UnauthorizedOperationException.class)
    public void unauthorizedUserCreation() {
        CreateUser service = new CreateUser(ars, USERNAME_DOES_NOT_EXIST, "jose@tecnico.pt", "José Ferreira");
        
        service.execute();
    }

    @Test(expected = UserNotInSessionException.class)
    public void accessUsernameNotExist() {
        removeUserFromSession(root);
        CreateUser service = new CreateUser(root, USERNAME_DOES_NOT_EXIST, "jose@tecnico.pt", "José Ferreira");

        service.execute();
    }
}
