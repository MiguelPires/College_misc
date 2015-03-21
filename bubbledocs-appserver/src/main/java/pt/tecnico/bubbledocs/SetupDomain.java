package pt.tecnico.bubbledocs;

import pt.tecnico.bubbledocs.domain.*;
import pt.tecnico.bubbledocs.service.AssignLiteralCell;
import pt.tecnico.bubbledocs.service.AssignReferenceCell;
import pt.tecnico.bubbledocs.service.CreateSpreadSheet;
import pt.tecnico.bubbledocs.service.CreateUser;
import pt.tecnico.bubbledocs.service.LoginUser;


public class SetupDomain {


    public static void main(String[] args) {
        populateDomain();
    }

    public static void populateDomain() {
    	LoginUser service = new LoginUser("root", "root");
    	service.execute();
    	String rootToken = service.getUserToken();
    	
    	CreateUser service2 = new CreateUser(rootToken, "pf", "sub", "Paul Door");
    	service2.execute();
    	LoginUser service3 = new LoginUser("pf", "sub");
    	service3.execute();
    	String pfToken = service3.getUserToken();

    	CreateUser service4 = new CreateUser(rootToken, "ra", "cor", "Step Rabbit");
    	service4.execute();
    	
    	CreateSpreadSheet service5 = new CreateSpreadSheet(pfToken, "Notas ES", 300, 20);
    	service5.execute();
    	int id = service5.getID();
    	
    	AssignLiteralCell service6 = new AssignLiteralCell(pfToken, id, "3;4", "5");
    	service6.execute();
    	AssignReferenceCell service7 = new AssignReferenceCell(pfToken, id, "1;1", "5;6");
    	service7.execute();
    	
    	Spreadsheet doc = Bubbledocs.getInstance().getSpreadsheet(id);
    	doc.addCellContent(5, 6, new Addition(new Literal(2), new Reference(doc.getCell(3, 4))));
        doc.addCellContent(2, 2,
                new Division(new Reference(doc.getCell(1, 1)), new Reference(doc.getCell(3, 4))));
    }

}
