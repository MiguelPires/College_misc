package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;

public class DeleteUser extends CheckLogin {

    private String deleteUsername;

    public DeleteUser(String userToken, String toDeleteUsername) {
        this.userToken = userToken;
        this.deleteUsername = toDeleteUsername;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
        super.dispatch();

        if (user.isRoot()) {
            deleteUser(deleteUsername);
        } else
            throw new UnauthorizedOperationException();
    }
}
