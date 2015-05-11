package pt.tecnico.bubbledocs.service.integration;

import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.service.DeleteUser;
import pt.tecnico.bubbledocs.service.GetUserInfo;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;

public class DeleteUserIntegrator extends BubbleDocsIntegrator {

    private String deleteUsername;
    private String userToken;

    private IDRemoteServices remote;
    private DeleteUser service;
    private GetUserInfo infoService;

    public DeleteUserIntegrator(String userToken, String toDeleteUsername) {
        this.userToken = userToken;
        this.deleteUsername = toDeleteUsername;
    }

    @Override
    public void dispatch() throws BubbleDocsException {
        infoService = new GetUserInfo(deleteUsername);
        infoService.execute();
        remote = new IDRemoteServices();
        service = new DeleteUser(userToken, deleteUsername);
        service.execute();

        try {
            remote.removeUser(deleteUsername);
        } catch (RemoteInvocationException e) {
            createUser(infoService.getUsername(), infoService.getName(), infoService.getEmail());
            throw new UnavailableServiceException();
        }
    }

}
