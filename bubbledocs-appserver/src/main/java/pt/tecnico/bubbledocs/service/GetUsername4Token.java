package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.EmptyUsernameException;

public class GetUsername4Token extends CheckLogin {

	private User user;
    private String username;
    private String userToken;

    public GetUsername4Token(String userToken) {
		this.userToken = userToken;
    }
    
    public String getUsername() {
    	return username;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
    	if(userToken == null || userToken.isEmpty())
    		throw new EmptyUsernameException();
    	
    	 super.dispatch();
    	 this.username = user.getUsername();
    }
}
