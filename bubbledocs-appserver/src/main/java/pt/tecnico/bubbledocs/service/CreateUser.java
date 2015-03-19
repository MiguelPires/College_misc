package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;

public class CreateUser extends BubbleDocsService {

    private String token;
    private String username;
    private String password;
    private String name;
    
    public CreateUser(String userToken, String newUsername,
            String password, String name) {
        this.token = userToken;
        this.username = newUsername;
        this.password = password;
        this.name = name;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
        User user = getUserByToken(token);
        
        if (user.isRoot())
            createUser(username, name, password);
        else
            throw new UnauthorizedOperationException();
    }
}

