package pt.tecnico.bubbledocs.service.integration;

import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.service.remote.StoreRemoteServices;
import pt.tecnico.bubbledocs.service.GetUsername4Token;
import pt.tecnico.bubbledocs.service.ImportDocument;

public class ImportDocumentIntegrator extends BubbleDocsIntegrator {

	private String userToken;
	private String username;
	private Spreadsheet doc;
	private byte[] ssByte;
	StoreRemoteServices remoteService;
	ImportDocument localService;

	public ImportDocumentIntegrator(int docId, String userToken) {
		this.userToken = userToken;
	}

	public org.jdom2.Document getDocXML() {
		return localService.getDocXML();
	}

	protected void dispatch() throws BubbleDocsException {
		remoteService = new StoreRemoteServices();

		GetUsername4Token getUsernameService = new GetUsername4Token(userToken);
		username = getUsernameService.getUsername();

		try {
			ssByte = remoteService.loadDocument(username, doc.getName());
			localService = new ImportDocument(ssByte, userToken);
			localService.execute();
		} catch (RemoteInvocationException e) {
			throw new UnavailableServiceException();
		}
	}

}