package pt.tecnico.bubbledocs.service.integration;

import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.service.GetUsername4Token;
import pt.tecnico.bubbledocs.service.ImportDocument;
import pt.tecnico.bubbledocs.service.remote.StoreRemoteServices;

public class ImportDocumentIntegrator extends BubbleDocsIntegrator {

    private String userToken;
    private String username;
    private String doc;
    private byte[] ssByte;
    StoreRemoteServices remoteService;
    ImportDocument localService;

    public ImportDocumentIntegrator(int docId, String userToken) {
        this.userToken = userToken;
        this.doc = (new Integer(docId)).toString();
    }

    public org.jdom2.Document getDocXML() {
        return localService.getDocXML();
    }

    public Spreadsheet getSpreadsheet() {
        return localService.getSpreadsheet();
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
        remoteService = new StoreRemoteServices();

        GetUsername4Token getUsernameService = new GetUsername4Token(userToken);
        username = getUsernameService.getUsername();

        try {
            ssByte = remoteService.loadDocument(username, doc);
            localService = new ImportDocument(ssByte, userToken);
            localService.execute();
        } catch (RemoteInvocationException e) {
            throw new UnavailableServiceException();
        }
    }
}
