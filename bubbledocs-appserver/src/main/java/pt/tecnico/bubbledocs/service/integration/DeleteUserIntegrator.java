package pt.tecnico.bubbledocs.service.integration;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.service.DeleteUser;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;

public class DeleteUserIntegrator extends BubbleDocsIntegrator {

	 private String deleteUsername;
	 private String userToken;
	 
	 private DeleteUser service;
	 private User user;
	 
	 public DeleteUserIntegrator(String userToken, String toDeleteUsername) {
	        this.userToken = userToken;
	        this.deleteUsername = toDeleteUsername;
	 }
	 
	 public void dispatch() throws BubbleDocsException {
		 IDRemoteServices remote = new IDRemoteServices();
		 service = new DeleteUser(userToken, deleteUsername);

	        if (user.isRoot()) {
	            try {
	                remote.removeUser(deleteUsername);
	                service.execute();
	            } catch (RemoteInvocationException e) {
	                throw new UnavailableServiceException();
	            }

	            removeUser(deleteUsername);
	        } else
	            throw new UnauthorizedOperationException();
		 
	 }
	 
}
