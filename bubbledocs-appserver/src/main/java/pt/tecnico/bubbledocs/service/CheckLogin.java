package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;

public class CheckLogin extends BubbleDocsService {
    protected User user;
    protected String userToken;

    public CheckLogin() {
        user = null;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
        user = getUserByToken(userToken);

        if (!isLoggedIn(user))
            throw new UserNotInSessionException();
    }
}
