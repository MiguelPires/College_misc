package pt.tecnico.bubbledocs.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

import org.junit.Test;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.DuplicateUsernameException;
import pt.tecnico.bubbledocs.exception.EmptyUsernameException;
import pt.tecnico.bubbledocs.exception.InvalidEmailException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;

public class RenewPasswordTest extends BubbleDocsServiceTest{
	
	private String ars;
	
	private static final String USERNAME = "ars";
    private static final String EMAIL = "ars@tecnico.pt";
    private static final String NAME = "António Rito Silva";
	
	private IDRemoteServices serviceId;
	
	@Override
	public void populate4Test() throws BubbleDocsException {
		createUser(USERNAME, EMAIL, NAME);
		ars = addUserToSession(USERNAME);
	}
	
	@Test
	public void success() {
		new MockUp<IDRemoteServices>() {
			@Mock
			public void renewPassword(String username) {
				//
			}
		};
		
		RenewPassword service = new RenewPassword(ars);
		service.execute();

		User user = getUserFromUsername(USERNAME);
		assertNull("Password renewed: no local copy.", user.getPassword());
	}
	
	@Test(expected = UserNotInSessionException.class)
    public void unauthorizedRenewalUserNotInSession() {
    	removeUserFromSession(ars);
    	RenewPassword service = new RenewPassword(ars);
        service.execute();
    }
	
	@Test(expected = UserNotInSessionException.class)
    public void unauthorizedRenewalUserDoesNotExist() {
    	RenewPassword service = new RenewPassword("ola");
        service.execute();
    }
}
