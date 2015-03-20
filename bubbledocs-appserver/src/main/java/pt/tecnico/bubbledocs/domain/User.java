package pt.tecnico.bubbledocs.domain;

import java.util.ArrayList;

import org.jdom2.Element;

import pt.tecnico.bubbledocs.exception.ImportDocumentException;

public class User extends User_Base {

    public User(String username, String name, String password) {
        super();
        setUsername(username);
        setName(name);
        setPassword(password);
    }

    public User() { 
        super();
    }

    @Override
    public void setForbiddenBubble1(Bubbledocs forbiddenBubble1) {
        forbiddenBubble1.addUsers(this);
    }
    
    public ArrayList<Spreadsheet> getCreatedDocs(String name) {
        ArrayList<Spreadsheet> documents = new ArrayList<Spreadsheet>();
        for (Spreadsheet doc : this.getCreatedDocsSet())
            if (doc.getName().equals(name))
                documents.add(doc);
        return documents;
    }

    public void importFromXML(Element userElement) throws ImportDocumentException {

        setUsername(userElement.getAttribute("username").getValue());
        setName(userElement.getAttribute("name").getValue());
        setPassword(userElement.getAttribute("password").getValue());

    }

    public Element exportToXML() {
        Element element = new Element("user");

        element.setAttribute("username", getUsername());
        element.setAttribute("name", getName());
        element.setAttribute("password", getPassword());

        return element;
    }

    public void delete() {
        for (Spreadsheet doc : getCreatedDocsSet()) {
            removeCreatedDocs(doc);
            removeWritableDocs(doc);
            doc.delete();
        }

        for (Spreadsheet doc : getReadableDocsSet()) {
            removeReadableDocs(doc);
        }

        super.setForbiddenBubble1(null);

        ActiveUser actUser = getActiveUser();

        if (actUser != null)
            actUser.delete();

        setActiveUser(null);
        deleteDomainObject();
    }


    public boolean isRoot() {
        if (getUsername().equals("root"))
            return true;
        return false;
    }
}
