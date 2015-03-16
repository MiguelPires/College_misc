package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.exception.BubbleDocsException;

// add needed import declarations

public class LoginUser extends BubbleDocsService {

    private String userToken;

    public LoginUser(String username, String password) {
	// add code here
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
	// add code here
    }

    public final String getUserToken() {
	return userToken;
    }
}
