package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;

public class CreateUser extends CheckLogin {

    private String username;
    private String name;
    private String email;

    public CreateUser(String userToken, String newUsername, String email, String name) {
        this.userToken = userToken;
        this.username = newUsername;
        this.name = name;
        this.email = email;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
        IDRemoteServices remote = new IDRemoteServices();
        super.dispatch();

        if (user.isRoot()) {
            try {
                remote.createUser(username, email);
            } catch (RemoteInvocationException e) {
                throw new UnavailableServiceException();
            }

            createUser(username, name, email);
        } else
            throw new UnauthorizedOperationException();
    }
}
