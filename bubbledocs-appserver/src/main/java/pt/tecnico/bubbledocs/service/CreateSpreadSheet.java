package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.ActiveUser;
import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;

public class CreateSpreadSheet extends BubbleDocsService {
    private int sheetId;  // id of the new sheet
    private int rows;
    private int columns;
    private String token;
    private String name;

    public int getSheetId() {
        return sheetId;
    }
    
    public int getSheetRows() {
    	return rows;
    }
    
    public int getSheetCol() {
    	return columns;
    }
    
    public String getSheetName() {
    	return name;
    }
    
    public CreateSpreadSheet(String userToken, String name, int rows,
            int columns) {
        this.token = userToken;
        this.name = name;
        this.rows = rows;
        this.columns = columns;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
        User user = getUserByToken(token);

        try{

            ActiveUser active = getActiveUserByUsername(user.getUsername());

            Spreadsheet ss = createSpreadSheet(user, name, rows, columns);
            sheetId = ss.getID();

        } catch (UserNotInSessionException e){
            ; //not found if user isn't logged in
        }
        
    }

}
