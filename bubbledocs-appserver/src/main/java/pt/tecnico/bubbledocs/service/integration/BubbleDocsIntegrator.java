package pt.tecnico.bubbledocs.service.integration;

import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.service.BubbleDocsService;

public abstract class BubbleDocsIntegrator {
    
    public final void execute() throws BubbleDocsException {
        dispatch();
    }

    protected abstract void dispatch() throws BubbleDocsException;
    
    protected void removeUser(String username) {
        BubbleDocsService.getBubbledocs().removeUser(username);
    }
    
    protected String addUserToSession(User user){
    	return BubbleDocsService.getBubbledocs().addUserToSession(user); 
    }
    
    protected void validatePassword(String username){
    	BubbleDocsService.validatePassword(username); 
    }
    
    protected Spreadsheet getSpreadsheet(int docId) {
    	return BubbleDocsService.getSpreadsheet(docId);
    }
}
