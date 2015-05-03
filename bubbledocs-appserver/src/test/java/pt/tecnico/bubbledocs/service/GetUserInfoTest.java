package pt.tecnico.bubbledocs.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;
import pt.tecnico.bubbledocs.service.BubbleDocsServiceTest;


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
    	GetUserInfo service = new GetUserInfo(USERNAME);
    	service.execute();
    	
    	assertEquals(USERNAME, service.getUsername());
        assertEquals(EMAIL, service.getEmail());
        assertEquals("António Rito Silva", service.getName());
    	
    }
    
    @Test(expected = UserNotInSessionException.class)
    public void userDoesNotExist(){
    	GetUserInfo service = new GetUserInfo(USERNAME_DOES_NOT_EXIST);
        service.execute();
    }

     @Test(expected = UserNotInSessionException.class)
    public void nullUsername(){
        GetUserInfo service = new GetUserInfo(null);
        service.execute();
    }

}
