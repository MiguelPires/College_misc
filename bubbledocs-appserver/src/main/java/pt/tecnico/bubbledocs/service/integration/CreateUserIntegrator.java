package pt.tecnico.bubbledocs.service.integration;

import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.service.CreateUser;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;

public class CreateUserIntegrator extends BubbleDocsIntegrator{

    private String token;
    private String username;
    private String name;
    private String email;
    
    public CreateUserIntegrator(String userToken, String newUsername, String email, String name) {
        this.token = userToken;
        this.username = newUsername;
        this.name = name;
        this.email = email;
    }
    
    @Override
    protected void dispatch() throws BubbleDocsException {
        CreateUser service = new CreateUser(token, username, email, name);
        service.execute();
       
        try {
            remoteCreateUser();
        } catch(UnavailableServiceException e) {
            removeUser(username);                       // compensate for local creation 
            throw new UnavailableServiceException();    // rethrow exception
        }
    }

    private void remoteCreateUser() {
        IDRemoteServices remote = new IDRemoteServices();
        try {
            remote.createUser(username, email);
        } catch (RemoteInvocationException e) {
            throw new UnavailableServiceException();
        }
    }
}
