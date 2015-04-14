package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;

public class DeleteUser extends BubbleDocsService {

    private String userToken;
    private String deleteUsername;

    public DeleteUser(String userToken, String toDeleteUsername) {
        this.userToken = userToken;
        this.deleteUsername = toDeleteUsername;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
    	IDRemoteServices remote = new IDRemoteServices();
        User user = checkLogin(userToken);
        
        if (user.isRoot()) {
        	try{
        	remote.removeUser(deleteUsername);
        	} catch (RemoteInvocationException e) {
        		throw new UnavailableServiceException();
        	}

            getBubbledocs().removeUser(deleteUsername);
        } else
            throw new UnauthorizedOperationException();
    }

}
