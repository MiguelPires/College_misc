package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;

public class CreateSpreadSheet extends BubbleDocsService {
    private int rows;
    private int columns;
    private int id;
    private String token;
    private String name;

    public CreateSpreadSheet(String userToken, String name, int rows, int columns) {
        this.token = userToken;
        this.name = name;
        this.rows = rows;
        this.columns = columns;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
        User user = checkLogin(token);

        Spreadsheet sp = createSpreadsheet(user, name, rows, columns);
        this.id = sp.getID();
    }

    public int getID() {
        return this.id;
    }

}
