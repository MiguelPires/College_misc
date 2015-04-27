package pt.tecnico.bubbledocs.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;
import pt.tecnico.bubbledocs.service.BubbleDocsServiceTest;
import pt.tecnico.bubbledocs.service.CreateUser;



public class GetUserInfoTest extends BubbleDocsServiceTest {
	
	private static final String USERNAME = "ars";
    private static final String EMAIL = "ars@tecnico.pt";
    private static final String ROOT_USERNAME = "root";
    private static final String USERNAME_DOES_NOT_EXIST = "no-one";
	
	private String root;
    private String ars;
    
    @Override
    public void populate4Test() throws BubbleDocsException {
        createUser(USERNAME, EMAIL, "António Rito Silva");
        root = addUserToSession(ROOT_USERNAME);
        ars = addUserToSession("ars");
    }
    
    @Test
    public void success() throws BubbleDocsException {
    	User user = getUserFromUsername(USERNAME);

        GetUserInfo service = new GetUserInfo(user);
    	service.execute();
    	
    	assertEquals(USERNAME, user.getUsername());
        assertEquals(EMAIL, user.getEmail());
        assertEquals("António Rito Silva", user.getName());
    	
    }
    
    @Test(expected = UserNotInSessionException.class)
    public void userDoesNotExist(){
    	User user = getUserFromUsername(USERNAME_DOES_NOT_EXIST);

        GetUserInfo service = new GetUserInfo(user);
        service.execute();
    }

     @Test(expected = UserNotInSessionException.class)
    public void nullUser(){
        User user = null;

        GetUserInfo service = new GetUserInfo(user);
        service.execute();
    }

}
