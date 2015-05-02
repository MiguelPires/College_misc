package pt.tecnico.bubbledocs.service.integration;

import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.LoginBubbleDocsException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.service.RenewPassword;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;

public class RenewPasswordIntegrator extends BubbleDocsIntegrator{
	private String userToken;
	private String username;
	private RenewPassword service;
		
    public RenewPasswordIntegrator(String userToken) {
        this.userToken = userToken;
    }
    
    @Override
    protected void dispatch() throws BubbleDocsException, LoginBubbleDocsException {
        IDRemoteServices remote = new IDRemoteServices();
        service = new RenewPassword(userToken);
        
        try{
        	username = service.getUsername();
        	service.execute();
        	remote.renewPassword(username);
        }
        catch(RemoteInvocationException e){
        	validatePassword(username);
        }
    }
}