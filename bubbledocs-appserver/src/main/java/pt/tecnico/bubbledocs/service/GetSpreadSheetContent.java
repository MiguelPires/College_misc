package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;


public class GetSpreadSheetContent extends CheckLogin {

	private User user;
    private int docId;
    private String[][] result;

    public GetSpreadSheetContent(int id, String token) {
        this.user = getUserByToken(token);
        this.docId = id;
    }
    
    @Override
    protected void dispatch() throws BubbleDocsException {
    	super.dispatch();
    	
    	Spreadsheet ss = getSpreadsheet(docId);

        if (ss.getCreator().equals(user) || ss.isWriter(user.getUsername()) || ss.isReader(user.getUsername())) {
            result = ss.getSpreadsheetContent();
        } else
            throw new UnauthorizedOperationException();
    }
    
    public String[][] getResult() {
    	return result;
    }
}
