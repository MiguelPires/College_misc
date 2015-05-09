package pt.tecnico.bubbledocs.service.integration;

import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnavailableServiceException;
import pt.tecnico.bubbledocs.service.remote.StoreRemoteServices;
import pt.tecnico.bubbledocs.service.GetUsername4Token;
import pt.tecnico.bubbledocs.service.ImportDocument;
import pt.tecnico.bubbledocs.service.integration.CreateSpreadSheetIntegrator;


public class ImportDocumentIntegrator extends BubbleDocsIntegrator {
    
  private String userToken;
  private String username;
  private Spreadsheet doc;
  private int docId;
  private Spreadsheet newDoc;
  private int newDocId;
  
  StoreRemoteServices remote;
  ImportDocument importService;
  
   
  public ImportDocumentIntegrator(int docId, String userToken) {
      this.docId = docId;
      this.userToken = userToken;
  }

  public byte[] convertToBytes(org.jdom2.Document doc) {
        XMLOutputter xml = new XMLOutputter();
        xml.setFormat(Format.getPrettyFormat());
        String str = xml.outputString(doc);
        return str.getBytes();
    }

  protected void dispatch() throws BubbleDocsException {
    remote = new StoreRemoteServices();
    importService = new ImportDocument(docId, userToken);
    	
    GetUsername4Token getUsernameService = new GetUsername4Token(userToken);
    username = getUsernameService.getUsername();

    doc = getSpreadsheet(docId);
    	
    try {
    	remote.loadDocument(username, doc.getName());
   		importService.execute();
   	} catch (RemoteInvocationException e) {
   		throw new UnavailableServiceException();
   	}
    
    CreateSpreadSheetIntegrator createSpreadSheet = new CreateSpreadSheetIntegrator(userToken, doc.getName(), doc.getRows(), doc.getColumns());
  
    newDocId = createSpreadSheet.getID();
    newDoc = getSpreadsheet(newDocId);
    
    try {
      remote.storeDocument(userToken, newDoc.getName(), convertToBytes(importService.getDocXML()));
    } catch (RemoteInvocationException e) {
      throw new UnavailableServiceException("The storage service is unavailable");
    }

  }
  
  public org.jdom2.Document getDocXML() {
      return importService.getDocXML();
  }
    
}    