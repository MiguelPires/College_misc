package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;

public class RenewPassword extends CheckLogin {

    public RenewPassword(String userToken) {
        this.userToken = userToken;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
        IDRemoteServices remote = new IDRemoteServices();
        super.dispatch();

        try {
            remote.renewPassword(user.getUsername());
        } catch (RemoteInvocationException e) {
            throw new UnavailableServiceException();
        }

        renewPassword(user);
        removeUserByToken(userToken);
    }
}
