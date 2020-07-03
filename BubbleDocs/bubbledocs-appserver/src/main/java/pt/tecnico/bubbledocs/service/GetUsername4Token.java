package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.exception.BubbleDocsException;

public class GetUsername4Token extends CheckLogin {

    public GetUsername4Token(String userToken) {
		this.userToken = userToken;
    }
    
    public String getUsername() {
    	return user.getUsername();
    }

    @Override
    protected void dispatch() throws BubbleDocsException {   
    	super.dispatch();
    }
}
