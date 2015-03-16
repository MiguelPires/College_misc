package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.exception.BubbleDocsException;

// add needed import declarations

public class CreateSpreadSheet extends BubbleDocsService {
    private int sheetId;  // id of the new sheet

    public int getSheetId() {
        return sheetId;
    }

    public CreateSpreadSheet(String userToken, String name, int rows,
            int columns) {
	// add code here
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
	// add code here
    }

}
