package pt.tecnico.bubbledocs.service.integration;

import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.service.ExportDocument;
import pt.tecnico.bubbledocs.service.remote.StoreRemoteServices;

public class ExportDocumentIntegrator extends BubbleDocsIntegrator {
	private String userToken;
	private int docId;

	private StoreRemoteServices storeService;
	private ExportDocument localService;

	public byte[] convertToBytes(org.jdom2.Document doc) {
		XMLOutputter xml = new XMLOutputter();
		xml.setFormat(Format.getPrettyFormat());
		String str = xml.outputString(doc);
		return str.getBytes();
	}

	public ExportDocumentIntegrator(String userToken, int docId) {
		this.userToken = userToken;
		this.docId = docId;
		this.localService = new ExportDocument(userToken, docId);
		this.storeService = new StoreRemoteServices();
	}

	protected void dispatch() {

		localService.execute();
		try {
			storeService.storeDocument(userToken, Integer.toString(docId),
					convertToBytes(localService.getDocXML()));
		} catch (RemoteInvocationException e) {
			throw new UnavailableServiceException(
					"The storage service is unavailable");
		}
	}

	public org.jdom2.Document getDocXML() {
		return localService.getDocXML();
	}
}