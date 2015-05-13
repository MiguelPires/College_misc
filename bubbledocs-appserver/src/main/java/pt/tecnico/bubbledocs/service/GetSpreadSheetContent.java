package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;


public class GetSpreadSheetContent extends CheckLogin {

	private String username;
    private int docId;
    private String[][] result;

    public GetSpreadSheetContent(int id, String token) {
    	GetUsername4Token service = new GetUsername4Token(token);
    	service.execute(); 
    	
    	this.userToken = token;
    	this.username = service.getUsername();
        this.docId = id;
    }
    
    @Override
    protected void dispatch() throws BubbleDocsException {
    	super.dispatch();
    	
    	Spreadsheet ss = getSpreadsheet(docId);
        if (ss.getCreator().getUsername().equals(username) || ss.isWriter(username) || ss.isReader(username)) {
        	result = ss.getSpreadsheetContent();
        } else
            throw new UnauthorizedOperationException();
    }
    
    public String[][] getResult() {
    	return result;
    }
}
