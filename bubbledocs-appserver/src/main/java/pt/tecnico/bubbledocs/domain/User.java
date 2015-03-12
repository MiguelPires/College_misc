package pt.tecnico.bubbledocs.domain;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import pt.tecnico.bubbledocs.exception.ImportDocumentException;


public class User extends User_Base {
    
    public User(String username, String name, String password) {
		super();
        setUsername(username);
        setName(name);
        setPassword(password);
    }
	
	User(){
	}
    
	public ArrayList<Spreadsheet> findCreatedDocs(String name){
		ArrayList<Spreadsheet> documents = new ArrayList<Spreadsheet>();
		for (Spreadsheet doc : this.getCreatedDocsSet())
			if(doc.getName().equals(name))
				documents.add(doc);
				
		return documents;
	}
	
	public void importFromXML(Element userElement) throws ImportDocumentException{
		setUsername(userElement.getAttribute("username").getValue());
		try {
		    setName(userElement.getAttribute("name").getValue());
		    setPassword(userElement.getAttribute("password").getValue());
		} catch (ImportDocumentException e) { 
		    e.importDocException();
		}
	}
	
	public Element exportToXML() {
		Element element = new Element("users");
		element.setAttribute("username",  getUsername());
		element.setAttribute("name", getName());
		element.setAttribute("password", getPassword());
		
		Element spreadsheetElement = new Element("spreadsheet");
		element.addContent(spreadsheetElement);

		for (Spreadsheet c : getCreatedDocsSet()) {
			spreadsheetElement.addContent(c.exportToXML());
		}
		
		return element;
	}
}

