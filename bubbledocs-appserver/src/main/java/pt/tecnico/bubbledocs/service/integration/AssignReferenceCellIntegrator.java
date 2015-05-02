package pt.tecnico.bubbledocs.service.integration;

import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.service.AssignReferenceCell;

public class AssignReferenceCellIntegrator {
	private String result;
	
	private AssignReferenceCell localService;
    
    public String getResult() {
        return this.result;
    }

    public AssignReferenceCellIntegrator(String userToken, int docId, String cellId, String reference) {
        localService = new AssignReferenceCell(userToken, docId, cellId, reference);
    }

    protected void dispatch() throws BubbleDocsException {
        localService.execute();
    }
}