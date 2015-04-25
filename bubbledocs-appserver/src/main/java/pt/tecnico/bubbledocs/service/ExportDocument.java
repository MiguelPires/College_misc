package pt.tecnico.bubbledocs.service;

import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.service.remote.StoreRemoteServices;

public class ExportDocument extends CheckLogin {
    private org.jdom2.Document docXML;
    private int docId;
    private StoreRemoteServices storeService;

    public byte[] convertToBytes(org.jdom2.Document doc) {
        XMLOutputter xml = new XMLOutputter();
        xml.setFormat(Format.getPrettyFormat());
        String str = xml.outputString(doc);
        return str.getBytes();
    }

    public org.jdom2.Document getDocXML() {
        return docXML;
    }

    public ExportDocument(String userToken, int docId) {
        this.userToken = userToken;
        this.docId = docId;
        this.storeService = new StoreRemoteServices();
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
        super.dispatch();

        Spreadsheet ss = getSpreadsheet(docId);

        if (ss.getCreator().equals(user) || ss.isWriter(user.getUsername())
                || ss.isReader(user.getUsername())) {

            docXML = exportToXML(docId);
            try {
                storeService.storeDocument(userToken, ss.getName(), convertToBytes(docXML));
            } catch (RemoteInvocationException e) {
                throw new UnavailableServiceException("The storage service is unavailable");
            }
        } else
            throw new UnauthorizedOperationException();
    }
}
