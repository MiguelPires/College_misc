package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.exception.BubbleDocsException;

public class RenewPassword extends CheckLogin {

	private String username;
    public RenewPassword(String userToken) {
        this.userToken = userToken;
        this.username = user.getUsername();
    }
    
    public String getUsername(){
    	return username;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
        super.dispatch();
        renewPassword(user);
        removeUserByToken(userToken);
    }
}
