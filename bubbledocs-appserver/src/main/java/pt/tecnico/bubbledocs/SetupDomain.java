package pt.tecnico.bubbledocs;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.Config;

import org.joda.time.*;

import pt.tecnico.bubbledocs.domain.*;


public class SetupDomain {

    
    public static void main(String[] args) {
		populateDomain();
    }
    
    @Atomic
    static void populateDomain() {
		Bubbledocs bubbleapp = Bubbledocs.getInstance();

		User pf = new User("pf", "Paul Door", "sub");
		bubbleapp.addUsers(pf);
	
		User ra = new User("ra", "Step Rabbit", "cor");
		bubbleapp.addUsers(ra);

		Spreadsheet doc = pf.createSpreadsheet(bubbleapp.getLastID()+1, "Notas ES", 300, 20); //constructor ? falta o creator pf
		bubbleapp.addDocs(doc);
		
		doc.addCellContent(3, 4, new Literal(5));
		doc.addCellContent(1, 1, new Reference(doc.getCell(5, 6)));
		doc.addCellContent(5, 6, new Addition(new Literal(2), new Reference(doc.getCell(3, 4))));
		doc.addCellContent(2, 2, new Division(new Reference(doc.getCell(1, 1)), 
		                                     new Reference(doc.getCell(3, 4))));
		
    }

}

