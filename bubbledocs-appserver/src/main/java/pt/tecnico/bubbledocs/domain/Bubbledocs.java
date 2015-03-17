package pt.tecnico.bubbledocs.domain;

import java.util.ArrayList;

import org.jdom2.Element;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;
import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;
import pt.tecnico.bubbledocs.exception.SpreadsheetNotFoundException;
import pt.tecnico.bubbledocs.exception.UserNotFoundException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;

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
		setSession(new Session());
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
	
	// User functions
	
	public User addUser(String username, String name, String password)
	{
	    User user = new User(username, name, password);
	    addUsers(user);
	    return user;
	}
	public void addUserToSession(User user, String token)
	{
	    getSession().addUser(user, token);
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
	
	public User getUserByToken(String token) throws UserNotInSessionException
	{
	    return getSession().getUserByToken(token); 
	}
	
	public ActiveUser getActiveUserByUsername(String username) throws UserNotInSessionException
	{
	    return getSession().getActiveUserByUsername(username);
	}
	
	
	public void importFromXML(Element spreadsheetElement) {	    
		Spreadsheet spread = new Spreadsheet();
		spread.importFromXML(spreadsheetElement);
		
		addDocs(spread);
		addUsers(spread.getCreator());
	}
	
	public Element exportToXML() throws ShouldNotExecuteException {
		
		Element element = new Element("bubbledocs");

		element.setAttribute("lastID",  Integer.toString(getLastID()));

		Element docsElement = new Element("docs");
		element.addContent(docsElement);

		for (Spreadsheet c : getDocsSet()) {
		    docsElement.addContent(c.exportToXML());
		}
    		
		return element;
	}
	
	
}

