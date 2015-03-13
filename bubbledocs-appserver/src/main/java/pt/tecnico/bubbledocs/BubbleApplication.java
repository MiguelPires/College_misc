package pt.tecnico.bubbledocs;

import java.util.ArrayList;

import javax.transaction.*;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.joda.time.*;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.TransactionManager;
import pt.tecnico.bubbledocs.domain.*;
import pt.tecnico.bubbledocs.exception.UserNotFoundException;

public class BubbleApplication {

	private static Bubbledocs bubbleapp;
	
    public static void main(String[] args) {

    	System.out.println("+--------------------------------------------+");
    	System.out.println("+   Welcome to the Bubbledocs application!   +");
    	System.out.println("+--------------------------------------------+");
    	
    	
    	TransactionManager tm = FenixFramework.getTransactionManager();
    	boolean committed = false;

    	try {
    		tm.begin();

    		bubbleapp =  Bubbledocs.getInstance();
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
    	
    	//escrever cenas da Jane
    	
    	//aceder as spreadsheets, converter e escrever o resultado
    	ArrayList <org.jdom2.Document> docList = exportFeature();
    	
    	//remover a spreadsheet do pf
    	deleteSpreadsheet();
    	
    	//mais cenas tipos prints e o #raiopicobomba
    	
    	//importar spreadsheet
    	//importFromXML(docList.get(0));
    	
    	//org.jdom2.Document doc = convertToXMLPf();
    	
		
		
    	//printSpreadsheetsUserPf();
		//importToSpreadsheet (doc);
    	//printSpreadsheetsUserPf();
    	//convertToXMLPf();
    	
    
   }
	
    
 // setup the initial state if Bubbledocs is empty
    private static void setupIfNeed(Bubbledocs bubbleapp) {
		if (bubbleapp.getUsersSet().isEmpty())
		    SetupDomain.populateDomain();
    }
    
    
    
    
   /* @Atomic 
    public static org.jdom2.Document convertToXML() {
		Bubbledocs bubbleapp = Bubbledocs.getInstance();
	
		org.jdom2.Document jdomDoc = new org.jdom2.Document();

		jdomDoc.setRootElement(bubbleapp.exportToXML());

		return jdomDoc;
    }*/
    @Atomic
    private static ArrayList <org.jdom2.Document> exportFeature()
    {
    	ArrayList <org.jdom2.Document> docList = new ArrayList <org.jdom2.Document>();
    	try {
			User u = bubbleapp.findUser("pf");
			
			docList = new ArrayList <org.jdom2.Document>();
	    	org.jdom2.Document doc;
	    	
	    	for(Spreadsheet s : u.getCreatedDocsSet()){
	    		doc = exportToXML(s);
	    		docList.add(doc);
	    		printDomainInXML(doc);
	    	}
		} catch (UserNotFoundException e) {
			System.out.println(e.getMessage());
		}
    	return docList;
    }
    
    public static org.jdom2.Document exportToXML(Spreadsheet spreadsheet){
    	org.jdom2.Document jdomDoc = new org.jdom2.Document();
    	jdomDoc.setRootElement(spreadsheet.exportToXML());
    	
    	return jdomDoc;
    }
    
    public static void printDomainInXML(org.jdom2.Document jdomDoc) {
		XMLOutputter xml = new XMLOutputter();
		xml.setFormat(Format.getPrettyFormat());
		System.out.println(xml.outputString(jdomDoc));
    }
    

    @Atomic
    private static void importFromXML(org.jdom2.Document jdomDoc) {
		bubbleapp.importFromXML(jdomDoc.getRootElement());
    }
	
	
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