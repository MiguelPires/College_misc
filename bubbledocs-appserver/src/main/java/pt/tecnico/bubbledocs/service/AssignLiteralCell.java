package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.Cell;
import pt.tecnico.bubbledocs.domain.Literal;
import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;

public class AssignLiteralCell extends BubbleDocsService {
    private String result;
    private String token;
    private String cellId;
    private String literal;
    private int docId;
    private int row;
    private int column;
    private int l;
    
    public AssignLiteralCell(String userToken, int docId, String cellId, String literal) {
        this.token = userToken;
        this.cellId = cellId;
        this.literal = literal;
        this.docId = docId;
    }

    public int getRow(String cellId) {
        String intValue = cellId.replaceAll(";[0-9]*", "");
        return Integer.parseInt(intValue);
    }

    public int getColumn(String cellId) {
        String intValue = cellId.replaceAll("[0-9]*;", "");
        return Integer.parseInt(intValue);
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
        User user = checkLogin(token);

        Spreadsheet ss = getSpreadsheet(docId);
        
        if (ss.getCreator().equals(user) || ss.isWriter(user.getUsername())) {
            row = this.getRow(cellId);
            column = this.getColumn(cellId);

            try {
            	l = Integer.parseInt(literal);
            } catch (Exception e) {
            	throw new UnauthorizedOperationException("Wrong content " + literal + ".");
            }
            
            Literal content = new Literal(l);
            ss.addCellContent(row, column, content);

            Cell cell = ss.getCell(row, column);
            result = Integer.toString(cell.getContent().getValue());

        } else
            throw new UnauthorizedOperationException();
    }

    public String getResult() {
        return result;
    }
}
