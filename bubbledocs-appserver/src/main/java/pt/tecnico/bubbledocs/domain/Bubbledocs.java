package pt.tecnico.bubbledocs.domain;

import java.util.ArrayList;
import java.util.Random;

import org.jdom2.Element;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.DuplicateUsernameException;
import pt.tecnico.bubbledocs.exception.EmptyUsernameException;
import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;
import pt.tecnico.bubbledocs.exception.SpreadsheetNotFoundException;
import pt.tecnico.bubbledocs.exception.UnknownBubbleDocsUserException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;

public class Bubbledocs extends Bubbledocs_Base {
    
    @Atomic
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
		addUsers(new Root(this));
    }
	
    @Atomic
    public void removeUser(String username) throws UnknownBubbleDocsUserException{
        getUser(username).delete();
    }
    
    @Atomic
    public void removeUserByToken(String token) throws UserNotInSessionException{
        getSession().removeUser(token);
        
    }
    
	// User functions
    @Atomic
	public User createUser(String username, String name, String password) throws BubbleDocsException
	{
        if (username.isEmpty())
            throw new EmptyUsernameException();
        
        try {
            getUser(username);
            throw new DuplicateUsernameException();
        } catch(UnknownBubbleDocsUserException e)
        {
            User user = new User(username, name, password);
            addUsers(user);
            return user;
        }
	}
    
    @Atomic
	public String addUserToSession(String username) throws UnknownBubbleDocsUserException
	{
		 
		User user = getUser(username);
	    return addUserToSession(user);
	}
	
    @Atomic
	public String addUserToSession(User user)
	{
		Random random = new Random();
        Integer randInt = new Integer(random.nextInt(10));       
        String token = user.getUsername() + randInt.toString();
	    getSession().addUser(user, token);
	    return token;
	}
	
    @Atomic
	public User getUser(String username) throws UnknownBubbleDocsUserException
	{
		for(User user : getUsersSet()){
			if(user.getUsername().equals(username)){
				return user;
			}
		}
		throw new UnknownBubbleDocsUserException("User '" + username + "' not found.");
	}
	
    @Atomic
	public User getUserByToken(String token) throws UserNotInSessionException
	{
	    return getSession().getUserByToken(token); 
	}
	
    @Atomic
	public ActiveUser getActiveUserByUsername(String username) throws UserNotInSessionException
	{
	    return getSession().getActiveUserByUsername(username);
	}
	
    @Atomic
	public void importFromXML(Element spreadsheetElement) {	    
		Spreadsheet spread = new Spreadsheet();
		spread.importFromXML(spreadsheetElement);
		
		addDocs(spread);
		addUsers(spread.getCreator());
	}
	
    @Atomic
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
	
    @Atomic
	public Spreadsheet createSpreadSheet(User user, String name, int rows,
            int columns) {
		setLastID(getLastID()+1);
		Spreadsheet Doc = new Spreadsheet(getLastID(), name, rows, columns, user);
		addDocs(Doc);
    	return Doc;
    }
	
    @Atomic
    public ArrayList<Spreadsheet> getCreatedDocsByUser(User user, String name){
        return user.getCreatedDocs(name);
    }
    
    @Atomic
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
    
    @Atomic
    public void deleteDoc(Spreadsheet doc)
    {
        doc.delete();
        removeDocs(doc);
    }
    
    @Atomic
    public Spreadsheet getSpreadSheet(String name)
    {
        for (Spreadsheet doc: getDocsSet())
        {
            if (doc.getName().equals(name))
                return doc;
        }
        return null;
    }
	
}

