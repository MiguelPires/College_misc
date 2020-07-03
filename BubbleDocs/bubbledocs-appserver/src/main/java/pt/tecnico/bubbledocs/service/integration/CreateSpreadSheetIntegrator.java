package pt.tecnico.bubbledocs.service.integration;

import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.service.CreateSpreadSheet;

public class CreateSpreadSheetIntegrator extends BubbleDocsIntegrator {

    private int rows;
    private int columns;
    private String name;
    private String token;
    private CreateSpreadSheet service;

    public CreateSpreadSheetIntegrator(String userToken, String name, int rows, int columns) {
        this.token = userToken;
        this.name = name;
        this.rows = rows;
        this.columns = columns;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
        service = new CreateSpreadSheet(token, name, rows, columns);
        service.execute();
    }

    public int getID() {
        return service.getID();
    }
}
