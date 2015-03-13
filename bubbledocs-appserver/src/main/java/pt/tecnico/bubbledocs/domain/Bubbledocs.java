package pt.tecnico.bubbledocs.domain;

import java.util.ArrayList;

import org.jdom2.Element;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;
import pt.tecnico.bubbledocs.exception.SpreadsheetNotFoundException;
import pt.tecnico.bubbledocs.exception.UserNotFoundException;

public class Bubbledocs extends Bubbledocs_Base {
    
    
    public static Bubbledocs getInstance()
    {		
        Bubbledocs bubble = FenixFramework.getDomainRoot().getBubbledocs();
    	
		if (bubble == null)
		    bubble = new Bubbledocs();

		return bubble;
    }

    private Bubbledocs() {
        setLastID(0);
		FenixFramework.getDomainRoot().setBubbledocs(this);
    }
    
	public ArrayList<Spreadsheet> findCreatedDocsByUser(User user, String name){
		return user.findCreatedDocs(name);
	}
	
	public void deleteDoc(Integer id) throws SpreadsheetNotFoundException
	{
	    for (Spreadsheet s: getDocsSet())
	    {
	        if (id.equals(s.getID()))
	        {
	            deleteDoc(s);
	            return;
	        }
	    }
	    throw new SpreadsheetNotFoundException("No spreadsheet was found for the "+id.toString()+" identifier.");	    
	}
	
	public void deleteDoc(Spreadsheet doc)
	{
	    doc.delete();
	    removeDocs(doc);
	}
	
	public User findUser(String username) throws UserNotFoundException
	{
		for(User user : getUsersSet()){
			if(user.getUsername().equals(username)){
				return user;
			}
		}
		throw new UserNotFoundException("User ' " + username + " ' not found.");
	}
	
	public void importFromXML(Element spreadsheetElement) {
	    
	    if (spreadsheetElement == null)
	        System.out.println("Bubbledocs ");
	    
		Element doc = spreadsheetElement.getChild("spreadsheet");
		
		if (doc == null)
		    System.out.println("docs");
	       
		Spreadsheet spread = new Spreadsheet();
		spread.importFromXML(doc);
		addDocs (spread);
	
	}
	
	/*public Element exportToXML() {
		Element element = new Element("bubbledocs");
		element.setAttribute("lastID",  Integer.toString(getLastID()));
		
		
		Element userElement = new Element("users");
		element.addContent(userElement);

		for (User c : getUsersSet()) {
		    userElement.addContent(c.exportToXML());
		}
		
		Element docsElement = new Element("docs");
		element.addContent(docsElement);

		for (Spreadsheet c : getDocsSet()) {
		    docsElement.addContent(c.exportToXML());
		}
		
		return element;
	}*/
	
	
}

