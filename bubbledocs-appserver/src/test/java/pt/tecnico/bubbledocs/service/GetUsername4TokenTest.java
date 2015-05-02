package pt.tecnico.bubbledocs.service;

import org.junit.Test;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.service.*;
import pt.tecnico.bubbledocs.exception.*;
//import pt.tecnico.bubbledocs.integration.component.ImportDocument;

public class GetUsername4TokenTest {
	
	private static final String TOKEN = "arstoken";
	private static final String USERNAME = "ars";
    private static final String EMAIL = "ars@tecnico.pt";
    private static final String ROOT_USERNAME = "root";
    private static final String USERNAME_DOES_NOT_EXIST = "no-one";
	
	
    private User as;
    
    @Override
    public void populate4Test() throws BubbleDocsException {
        as = createUser(TOKEN, USERNAME, EMAIL, "António Rito Silva");
        root = addUserToSession(ROOT_USERNAME);
        ars = addUserToSession("ars");
        
    }
    
    @Test
    public void success() throws BubbleDocsException {
    	GetUsername4Token service = new GetUsername4Token(token);
    	
    	service.execute();
    	
    	assertEquals(as.getUsername(), service.getUsername(TOKEN));
    	
    }
    
    @Test(expected = UserNotInSessionException.class)
    public void userDidNotLoginYet(){
    	removeUserFromSession(as);
    	
    	GetUsername4Token service = new GetUsername4Token("token");
    	
    	service.execute();
    }
    
    @Test(expected = InvalidUsernameException.class)
    public void userDoesNotExist(){
    	
    	GetUsername4Token service = new GetUsername4Token("token_user_not_exist");
        service.execute();
    }
    
    @Test(expected = EmptyUsernameException.class)
    public void userNull(){
    	GetUsername4Token service = new GetUsername4Token(null);
        service.execute();
    }
    
    @Test(expected = EmptyUsernameException.class)
    public void userEmpty(){
    	GetUsername4Token service = new GetUsername4Token("");
        service.execute();
    }
    
    
    

}
