package pt.tecnico.bubbledocs.service.integration;

import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.LoginBubbleDocsException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;
import pt.tecnico.bubbledocs.service.GetUsername4Token;
import pt.tecnico.bubbledocs.service.RenewPassword;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;

public class RenewPasswordIntegrator extends BubbleDocsIntegrator {
    private String userToken;
    private String username;
    private RenewPassword service;

    public RenewPasswordIntegrator(String userToken) {
        GetUsername4Token service = new GetUsername4Token(userToken);
        service.execute();

        this.userToken = userToken;
        this.username = service.getUsername();
    }

    @Override
    protected void dispatch() throws BubbleDocsException, LoginBubbleDocsException, UserNotInSessionException {
        IDRemoteServices remote = new IDRemoteServices();
        service = new RenewPassword(userToken);

        try {
            service.execute();
            remote.renewPassword(username);
        } catch (RemoteInvocationException e) {
            validatePassword(username);
            throw new UnavailableServiceException();
        }
    }
}
