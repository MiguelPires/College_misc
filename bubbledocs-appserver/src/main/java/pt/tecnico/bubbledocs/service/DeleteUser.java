package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UnknownBubbleDocsUserException;

// add needed import declarations

public class DeleteUser extends BubbleDocsService {

	private String userToken;
	private String deleteUsername;
	
    public DeleteUser(String userToken, String toDeleteUsername) {
		this.userToken = userToken;
		this.deleteUsername = toDeleteUsername;
    }

    @Override
    protected void dispatch() throws BubbleDocsException{
    	User user = getUserByToken(userToken);
    	if(user.isRoot()){
				try {
					getBubbledocs().removeUser(deleteUsername);
				} catch (UnknownBubbleDocsUserException e) {
					throw new UnknownBubbleDocsUserException();
				}
    	}
    	else
    		throw new UnauthorizedOperationException();
    }

}
