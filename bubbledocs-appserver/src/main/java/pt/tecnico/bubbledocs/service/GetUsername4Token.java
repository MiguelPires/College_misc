package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;

public class GetUsername4Token extends CheckLogin {

	private User user;
    private String username;
    private String userToken;

    public GetUsername4Token(String userToken) {
		this.userToken = userToken;
        this.username = user.getUsername();
    }
    
    public String getUsername() {
    	return username;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
    	 super.dispatch();
    }
}
