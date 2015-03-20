package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.ActiveUser;
import pt.tecnico.bubbledocs.domain.Cell;
import pt.tecnico.bubbledocs.domain.Content;
import pt.tecnico.bubbledocs.domain.Literal;
import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;

public class AssignLiteralCell extends BubbleDocsService {
    private String result;
    private String token;
    private String cellId;
    private String literal;
    private int docId;
    private int row;
    private int column;

    public AssignLiteralCell(String userToken, int docId, String cellId,
            String literal) {
    	this.token = userToken;
		this.cellId = cellId;
		this.literal = literal;
		this.docId = docId;
    }
    
    public int getRow(String cellId){
    	String intValue = cellId.replaceAll(";[0-9]*", "");
    	return Integer.parseInt(intValue);
    }
    
    public int getColumn(String cellId){
    	String intValue = cellId.replaceAll("[0-9]*;", "");
    	return Integer.parseInt(intValue);
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
    	User user = getUserByToken(token);

        try{

            ActiveUser active = getActiveUserByUsername(user.getUsername());

        } catch (UserNotInSessionException e){
            ; //not found if user isn't logged in
        }
        
        Spreadsheet ss = getSpreadsheet(docId);
        
        if(ss.getCreator().equals(user) || 
     	  ss.isWriter(user.getUsername())) {
        	
        	row = this.getRow(cellId);
        	column = this.getColumn(cellId);
        	Literal content = new Literal(Integer.parseInt(literal));
        	ss.addCellContent(row, column, content);
        	Cell cell = ss.getCell(row, column);
        	try {
				result = Integer.toString(cell.getContent().getValue());
			} catch (ShouldNotExecuteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
     	   
        } else 
     	   throw new UnauthorizedOperationException();
    }

    public String getResult() {
        return result;
    }

}
