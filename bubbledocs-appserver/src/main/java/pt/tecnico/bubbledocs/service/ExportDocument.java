package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.ActiveUser;
import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;

public class ExportDocument extends BubbleDocsService {
    
    private byte[] docXML;
    private String token;
    private int docId;

    public byte[] getDocXML() {
        return docXML;
    }

    public ExportDocument(String userToken, int docId) {
	   this.token = userToken;
       this.docId = docId;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
	   User user = getUserByToken(token);

       try {
            ActiveUser active = getActiveUserByUsername(user.getUsername());
       
       } catch (UserNotInSessionException e){
            ; //not found if user isn't logged in
       }

       Spreadsheet ss = getSpreadsheet(docId);
       
       if(ss.getCreator().equals(user) || 
    	  ss.isWriter(user.getUsername()) ||
    	  ss.isReader(user.getUsername())) {
    	   
    	   try {
    		   exportToXML(docId);
    	   } catch (ShouldNotExecuteException e) {
    		   ;
    	   }
    	   
       } else 
    	   throw new UnauthorizedOperationException();
    }
}
