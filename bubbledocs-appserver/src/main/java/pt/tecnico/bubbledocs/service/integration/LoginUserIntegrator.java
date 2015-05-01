package pt.tecnico.bubbledocs.service.integration;

import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.LoginBubbleDocsException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.service.LoginUser;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;

public class LoginUserIntegrator extends BubbleDocsIntegrator {

	private String userToken;
    private String username;
    private String password;

    public LoginUserIntegrator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    protected void dispatch() throws BubbleDocsException, LoginBubbleDocsException {
        IDRemoteServices remote = new IDRemoteServices();
        LoginUser service = new LoginUser(username, password);
        service.deleteIfLogged();
        
        try{
        remote.loginUser(username, password);
        service.execute();
        }
        catch(RemoteInvocationException e){
        	String pass = service.getUserPassword();
        	 if (pass == null)
                 throw new UnavailableServiceException();
             else if (pass.equals(password))
                 userToken = addUserToSession(service.getUser());
             else
                 throw new LoginBubbleDocsException();	
        }
    }
    
    public final String getUserToken() {
        return userToken;
    }
    
}
