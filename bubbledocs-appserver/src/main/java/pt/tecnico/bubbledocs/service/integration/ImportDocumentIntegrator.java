package pt.tecnico.bubbledocs.service.integration;

import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.domain.Spreadsheet;

import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;

import pt.tecnico.bubbledocs.service.remote.StoreRemoteServices;
import pt.tecnico.bubbledocs.service.ImportDocument;


public class ImportDocumentIntegrator extends BubbleDocsIntegrator {
    
  private String userToken;
  private String username;
  private int docId;
	
  private Spreadsheet doc;
  
   
  public ImportDocumentIntegrator(int docId, String userToken) {
      this.docId = docId;
      this.userToken = userToken;
  }

  protected void dispatch() throws BubbleDocsException {
    StoreRemoteServices remote = new StoreRemoteServices();
    ImportDocument importService = new ImportDocument(docId, userToken);
    	
    GetUsername4Token getUsernameService = new GetUsername4Token(userToken);
    username = getUsernameService.getUsername();

    doc = getSpreadsheet(docId);
    	
    try {
    	remote.loadDocument(username, doc.getName());
   		importService.execute();
   	} catch (RemoteInvocationException e) {
   		throw new UnavailableServiceException();
   	}
  }
    
}    