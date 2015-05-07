package pt.tecnico.bubbledocs.service.integration;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
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
	 private User user;
	 
	public DeleteUserIntegrator(String userToken, String toDeleteUsername) {
	        this.userToken = userToken;
	        this.deleteUsername = toDeleteUsername;
	}
	 
	public void dispatch() throws BubbleDocsException {
		remote = new IDRemoteServices();
		service = new DeleteUser(userToken, deleteUsername);
		infoService = new GetUserInfo(user.getUsername());
		service.execute();
		 
		try {
			remote.removeUser(deleteUsername);        
		} catch (RemoteInvocationException e) {
			createUser(infoService.getUsername(), infoService.getName(), infoService.getEmail());
			throw new UnavailableServiceException();
		}		 
	 }
	 
}
