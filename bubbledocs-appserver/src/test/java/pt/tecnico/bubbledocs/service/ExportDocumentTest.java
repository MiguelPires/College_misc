package pt.tecnico.bubbledocs.service;

import static org.junit.Assert.assertEquals;


import org.junit.Test;

import pt.tecnico.bubbledocs.domain.Addition;
import pt.tecnico.bubbledocs.domain.Literal;
import pt.tecnico.bubbledocs.domain.Reference;
import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;

public class ExportDocumentTest extends BubbleDocsServiceTest{
    private String ars;
    private String mp;

    private static final String USERNAME = "ars";
    private static final String PASSWORD = "ars";

    private Spreadsheet doc;
    private User as;
    private String expectedFile = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
"<spreadsheet ID=\"1\" name=\"ES\" line=\"300\" column=\"20\">"+
"  <cells>"+
"    <cell line=\"3\" column=\"4\" protect=\"false\">"+
"      <content>"+
"        <literal value=\"5\" />"+
"      </content>"+
"    </cell>"+
"    <cell line=\"5\" column=\"6\" protect=\"false\">"+
"      <content>"+
"        <ADD>"+
"          <firstOperand>"+
"            <literal value=\"2\" />"+
"          </firstOperand>"+
"          <secondOperand>"+
"            <reference>"+
"              <cell line=\"3\" column=\"4\" protect=\"false\">"+
"                <content>"+
"                  <literal value=\"5\" />"+
"                </content>"+
"              </cell>"+
"            </reference>"+
"          </secondOperand>"+
"        </ADD>"+
"      </content>"+
"    </cell>"+
"  </cells>"+
"  <creator>"+
"    <user username=\"ars\" name=\"António Rito Silva\" password=\"ars\" />"+
"  </creator>"+
"</spreadsheet>";
    
    @Override
    public void populate4Test() throws BubbleDocsException {

        as = createUser(USERNAME, PASSWORD, "António Rito Silva");
        ars = addUserToSession("ars");
        doc = createSpreadSheet(as, "ES", 30, 100);

         createUser("mp", "1234", "Miguel Pires");
         mp = addUserToSession("mp");
        
        doc.addCellContent(3, 4, new Literal(5));   
        doc.addCellContent(5, 6, new Addition(new Literal(2), new Reference(doc.getCell(3, 4))));
         
    }

    @Test
    public void success() throws BubbleDocsException {
        
        ExportDocument service = new ExportDocument(ars, doc.getID());
        service.execute();
        assertEquals(expectedFile, service.getDocXML());       
        
    }
    
    @Test(expected=UnauthorizedOperationException.class)
    public void unauthorizedExport() throws BubbleDocsException
    {
        ExportDocument service = new ExportDocument(mp, doc.getID());
        service.execute();
    }
}
