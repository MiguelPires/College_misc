package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.Cell;
import pt.tecnico.bubbledocs.domain.Reference;
import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;

public class AssignReferenceCell extends BubbleDocsService {
    private int docId;
    private int cellRow;
    private int cellColumn;
    private int referenceRow;
    private int referenceColumn;
    
    private String result;
    private String cellId;
    private String userToken;
    private String reference;

    public AssignReferenceCell(String userToken, int docId, String cellId, String reference) {
        this.userToken = userToken;
        this.docId = docId;
        this.cellId = cellId;
        this.reference = reference;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
        User user = checkLogin(userToken);

        Spreadsheet doc = getBubbledocs().getSpreadsheet(docId);
        
        if (doc.getCreator().equals(user) || doc.isWriter(user.getUsername())) {
        	try {
        		String[] cellCoordinates = cellId.split(";");
        		cellRow = Integer.parseInt(cellCoordinates[0]);
        		cellColumn = Integer.parseInt(cellCoordinates[1]);

        		String[] referenceCoordinates = reference.split(";");
        		referenceRow = Integer.parseInt(referenceCoordinates[0]);
        		referenceColumn = Integer.parseInt(referenceCoordinates[1]);
        	} catch (Exception e) {
        		throw new UnauthorizedOperationException("Wrong content " + reference + ".");
        	}
            
            Cell reference = doc.getCell(referenceRow, referenceColumn);

            Reference ref = new Reference(reference);
            doc.addCellContent(cellRow, cellColumn, ref);
     
            Integer res = doc.getCell(cellRow, cellColumn).getContent().getValue();
            
            if (res == null)
                this.result = null;
            else
                this.result = res.toString();
        } else
            throw new UnauthorizedOperationException();
    }

    public String getResult() {
        return this.result;
    }


}
