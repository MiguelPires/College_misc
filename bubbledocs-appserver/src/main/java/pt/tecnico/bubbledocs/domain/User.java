package pt.tecnico.bubbledocs.domain;

import java.util.ArrayList;

import org.jdom2.Element;

import pt.tecnico.bubbledocs.exception.ImportDocumentException;
import pt.tecnico.bubbledocs.exception.InvalidSpreadsheetDimensionsException;
import pt.tecnico.bubbledocs.exception.InvalidUsernameException;
import pt.tecnico.bubbledocs.exception.SpreadsheetNotFoundException;

public class User extends User_Base {

    public User(String username, String name, String email) {
        super();
        
        if (username.length() < 3 || username.length() > 8) {
        	throw new InvalidUsernameException();
        }
        
        setUsername(username);
        setName(name);
        setEmail(email);
    }

    public User() { 
        super();
    }
    
    @Override
    public void addCreatedDocs(Spreadsheet doc) {
    	if (doc.getRows() < 1 || doc.getColumns() < 1)
            throw new InvalidSpreadsheetDimensionsException();
    	
       	super.addCreatedDocs(doc);
    }

    @Override
    public void setBubbleApp(Bubbledocs app) {
        app.addUsers(this);
    }
    
    public Spreadsheet createSpreadsheet(String name, int rows, int columns) {
        if (rows < 1 || columns < 1)
            throw new InvalidSpreadsheetDimensionsException();

        Spreadsheet doc = new Spreadsheet(getBubbleApp().getNewID(), name, rows, columns, this);
        addCreatedDocs(doc);
        return doc;
    }
    
    public ArrayList<Spreadsheet> getCreatedSpreadsheets(String name) {
        ArrayList<Spreadsheet> documents = new ArrayList<Spreadsheet>();
        for (Spreadsheet doc : this.getCreatedDocsSet())
            if (doc.getName().equals(name))
                documents.add(doc);
        return documents;
    }
    
    public Spreadsheet getSpreadsheet(int id) {
    	for (Spreadsheet spreadsheet : this.getCreatedDocsSet()) {
    		if (spreadsheet.getID() == id)
                return spreadsheet;
    	}
    	throw new SpreadsheetNotFoundException("No spreadsheet was found for the " + id
                + " identifier.");
    }
    
    public Spreadsheet getSpreadsheet(String name) {
    	for (Spreadsheet spreadsheet : this.getCreatedDocsSet()) {
    		if (spreadsheet.getName().equals(name))
                return spreadsheet;
    	}
    	throw new SpreadsheetNotFoundException("No spreadsheet was found for the \"" + name
                + "\" name.");
    }

    public void importFromXML(Element userElement) throws ImportDocumentException {
        setUsername(userElement.getAttribute("username").getValue());
        setName(userElement.getAttribute("name").getValue());
        setPassword(userElement.getAttribute("password").getValue());
        setEmail(userElement.getAttribute("email").getValue());
        
    }

    public Element exportToXML() {
        Element element = new Element("user");

        element.setAttribute("username", getUsername());
        element.setAttribute("name", getName());
        element.setAttribute("password", getPassword());
        element.setAttribute("email", getEmail());

        return element;
    }

    public void delete() {
        for (Spreadsheet doc : getCreatedDocsSet()) {
            removeCreatedDocs(doc);
            doc.delete();
        }
        
        setWritableDocs(null);
        setReadableDocs(null);
        super.setBubbleApp(null);

        ActiveUser actUser = getActiveUser();

        if (actUser != null)
            actUser.delete();

        setActiveUser(null);
        deleteDomainObject();
    }

    public boolean isRoot() {
        return false;
    }
}
