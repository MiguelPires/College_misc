package pt.tecnico.bubbledocs.service;

import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;

public class ExportDocument extends BubbleDocsService {

    private  org.jdom2.Document docXML;
    private String token;
    private int docId;

    public  org.jdom2.Document getDocXML() {
        return docXML;
    }

    public ExportDocument(String userToken, int docId) {
        this.token = userToken;
        this.docId = docId;
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
        User user = getUserByToken(token);

        if (!isLoggedIn(user))
            throw new UserNotInSessionException();

        Spreadsheet ss = getSpreadsheet(docId);

        if (ss.getCreator().equals(user) || ss.isWriter(user.getUsername())
                || ss.isReader(user.getUsername())) {

            docXML = exportToXML(docId);

        } else
            throw new UnauthorizedOperationException();
    }
}
