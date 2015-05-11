package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.exception.BubbleDocsException;

public class RenewPassword extends CheckLogin {

    private String username;

    public RenewPassword(String userToken) {
        this.userToken = userToken;
    }

    public String getUsername() {
        return username;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
        super.dispatch();
        this.username = user.getUsername();
        renewPassword(user);
        removeUserByToken(userToken);
    }
}
