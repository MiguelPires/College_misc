package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;


public class GetUserInfo extends BubbleDocsService {

	private User user;
	private String username;
    private String email;
    private String name;

    public GetUserInfo(String username) {
        this.username = username;
    }
    
    public String getUsername(){
    	return username;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
    	user = getUser(username);
    	this.name = user.getName();
    	this.email = user.getEmail();
    }
}
