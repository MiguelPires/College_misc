package pt.tecnico.bubbledocs.service.integration;

import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.EmptyUsernameException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.service.remote.StoreRemoteServices;
import pt.tecnico.bubbledocs.service.GetUsername4Token;
import pt.tecnico.bubbledocs.service.ImportDocument;


public class ImportDocumentIntegrator extends BubbleDocsIntegrator {
    
  private String userToken;
  private String username;
  private int docId;
  private byte[] content;
  private Spreadsheet doc;
  private ImportDocument importService;
   
  public ImportDocumentIntegrator(int docId, String userToken) {
      this.docId = docId;
      this.userToken = userToken;
  }

  protected void dispatch() throws BubbleDocsException {
	if(userToken==null || userToken.isEmpty())
		throw new EmptyUsernameException();
	  
    StoreRemoteServices remote = new StoreRemoteServices();
    importService = new ImportDocument(docId, userToken);
    	
    GetUsername4Token getUsernameService = new GetUsername4Token(userToken);
    username = getUsernameService.getUsername();

    doc = getSpreadsheet(docId);
    	
    try {
    	content = remote.loadDocument(username, docId+"");
   		importService.execute();
   	} catch (RemoteInvocationException e) {
   		throw new UnavailableServiceException();
   	}
  }
    
  public Spreadsheet getSpread(){
	  return importService.getSpread();
  }
}    