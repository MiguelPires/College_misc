package pt.tecnico.bubbledocs.service.integration;

import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.service.AssignLiteralCell;

public class AssignLiteralCellIntegrator extends BubbleDocsIntegrator {
    private String cellId;
    private String literal;
    private String userToken;
    private int docId;
    private AssignLiteralCell service;

    public AssignLiteralCellIntegrator(String userToken, int docId, String cellId, String literal) {
        this.userToken = userToken;
        this.cellId = cellId;
        this.literal = literal;
        this.docId = docId;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
       service = new AssignLiteralCell(userToken, docId, cellId, literal);
       service.execute();
    }

    public String getResult() {
        return service.getResult();
    }
}
