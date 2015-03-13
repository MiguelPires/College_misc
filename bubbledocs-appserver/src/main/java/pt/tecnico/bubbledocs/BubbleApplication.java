package pt.tecnico.bubbledocs;

import javax.transaction.*;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.joda.time.*;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.TransactionManager;

import pt.tecnico.bubbledocs.domain.*;

public class BubbleApplication {

    public static void main(String[] args) {

    	System.out.println("+--------------------------------------------+");
    	System.out.println("+   Welcome to the Bubbledocs application!   +");
    	System.out.println("+--------------------------------------------+");
    	
    	
    	TransactionManager tm = FenixFramework.getTransactionManager();
    	boolean committed = false;

    	try {
    		tm.begin();

    		Bubbledocs bubbleapp =  Bubbledocs.getInstance();
    		setupIfNeed(bubbleapp);

    		tm.commit();
    		committed = true;
    	} catch (SystemException| NotSupportedException | RollbackException| HeuristicMixedException | HeuristicRollbackException ex) {
    		System.err.println("Error in execution of transaction: " + ex);
    	} finally {
    		if (!committed)
    		{
    		   	try {
    				tm.rollback();
    			} catch (SystemException ex) {
    				System.err.println("Error in roll back of transaction: " + ex);
    			}	
    		} 

    	}
    	
    	//org.jdom2.Document doc = convertToXMLPf();
    	System.out.println("DELETE BEFORE");
		deleteSpreadsheet();
		System.out.println("DELETE AFTER");
    	printSpreadsheetsUserPf();
		//importToSpreadsheet (doc);
    	printSpreadsheetsUserPf();
    	//convertToXMLPf();
   }
	
    
 // setup the initial state if Bubbledocs is empty
    private static void setupIfNeed(Bubbledocs bubbleapp) {
		if (bubbleapp.getUsersSet().isEmpty())
		    SetupDomain.populateDomain();
    }
    
    
/*	@Atomic
    public static org.jdom2.Document convertToXMLPf() {
		Bubbledocs bubbleapp = new Bubbledocs();
		org.jdom2.Document jdomDoc = new org.jdom2.Document();
		XMLOutputter xml = new XMLOutputter();
		
		for (Spreadsheet s : bubbleapp.getDocsSet()) {
			if (s.getCreator().equals("pf")) {
				jdomDoc.setRootElement(s.exportToXML());
				xml.setFormat(Format.getPrettyFormat());
				System.out.println(xml.outputString(jdomDoc));
			}
		}
	}*/
	
	
    @Atomic
	public static void deleteSpreadsheet() {
		Bubbledocs bubbleapp = Bubbledocs.getInstance();
		for (Spreadsheet s : bubbleapp.getDocsSet()) {
			if (s.getCreator().equals("pf") && s.getName().equals("Notas ES")) {
				s.delete();
			}
		}
	}
	
	@Atomic
	public static void printSpreadsheetsUserPf() {
		Bubbledocs bubbleapp = Bubbledocs.getInstance();
		for (Spreadsheet s : bubbleapp.getDocsSet()) {
			if (s.getCreator().equals("pf")) {
				System.out.println("ID: " + s.getID() + " Name: " + s.getName());
			}
		}
	}	
	
	
/*	@Atomic
    public static void importToSpreadsheet (org.jdom2.Document doc) {
		Bubbledocs bubbleapp = new Bubbledocs();
		doc.setRootElement(bubbleapp.importToSpreadsheet());
	}*/
	

}