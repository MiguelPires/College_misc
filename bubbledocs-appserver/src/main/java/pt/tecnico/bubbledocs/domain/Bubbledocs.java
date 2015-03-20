package pt.tecnico.bubbledocs.domain;

import java.util.ArrayList;
import java.util.Random;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import pt.ist.fenixframework.FenixFramework;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.DuplicateUsernameException;
import pt.tecnico.bubbledocs.exception.EmptyUsernameException;
import pt.tecnico.bubbledocs.exception.InvalidSpreadsheetDimensionsException;
import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;
import pt.tecnico.bubbledocs.exception.SpreadsheetNotFoundException;
import pt.tecnico.bubbledocs.exception.UnknownBubbleDocsUserException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;

public class Bubbledocs extends Bubbledocs_Base {

    public static Bubbledocs getInstance() {
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

    public void removeUser(String username) throws UnknownBubbleDocsUserException {
        getUser(username).delete();
    }

    public void removeUserByToken(String token) throws UserNotInSessionException {
        getSession().removeUser(token);

    }

    public User createUser(String username, String name, String password) throws BubbleDocsException {
        User user = new User(username, name, password);
        try {
            addUsers(user);
        } catch (EmptyUsernameException e) {
            user.delete();
            throw new EmptyUsernameException();
        } catch (DuplicateUsernameException e) {
            user.delete();
            throw new DuplicateUsernameException();
        }
        return user;
    }

    @Override
    public void addUsers(User user) {
        if (user.getUsername().isEmpty())
            throw new EmptyUsernameException();

        try {
            getUser(user.getUsername());
            throw new DuplicateUsernameException();
        } catch (UnknownBubbleDocsUserException e) {
            super.addUsers(user);
        }
    }

    public String addUserToSession(String username) throws UnknownBubbleDocsUserException {
        User user = getUser(username);
        return addUserToSession(user);
    }

    public String addUserToSession(User user) {
        Random random = new Random();
        Integer randInt = new Integer(random.nextInt(10));
        String token = user.getUsername() + randInt.toString();
        getSession().addUser(user, token);
        return token;
    }

    public User getUser(String username) throws UnknownBubbleDocsUserException {
        for (User user : getUsersSet()) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        throw new UnknownBubbleDocsUserException("User '" + username + "' not found.");
    }

    public User getUserByToken(String token) throws UserNotInSessionException {
        return getSession().getUserByToken(token);
    }

    public ActiveUser getActiveUserByUsername(String username) throws UserNotInSessionException {
        return getSession().getActiveUserByUsername(username);
    }

    public void importFromXML(Element spreadsheetElement) {
        Spreadsheet spread = new Spreadsheet();
        spread.importFromXML(spreadsheetElement, this);

        addDocs(spread);
    }

    public org.jdom2.Document exportToXML(Spreadsheet spreadsheet) throws ShouldNotExecuteException {
        org.jdom2.Document jdomDoc = new org.jdom2.Document();
        jdomDoc.setRootElement(spreadsheet.exportToXML());
        return jdomDoc;
    }

    public org.jdom2.Document exportToXML(int id) throws ShouldNotExecuteException {
        return exportToXML(getSpreadsheet(id));
    }

    public String getDomainInXML(org.jdom2.Document jdomDoc) {
        XMLOutputter xml = new XMLOutputter();
        xml.setFormat(Format.getPrettyFormat());
        return xml.outputString(jdomDoc);
    }

    public Spreadsheet createSpreadsheet(User user, String name, int rows, int columns) {
        if (rows < 1 || columns < 1)
            throw new InvalidSpreadsheetDimensionsException();

        int id = getLastID() + 1;
        setLastID(id);
        Spreadsheet doc = new Spreadsheet(id, name, rows, columns, user);
        addDocs(doc);
        user.addCreatedDocs(doc);
        user.addWritableDocs(doc);
        return doc;
    }

    public ArrayList<Spreadsheet> getCreatedDocsByUser(User user, String name) {
        return user.getCreatedDocs(name);
    }

    public void deleteDoc(Integer id) throws SpreadsheetNotFoundException {
        for (Spreadsheet s : getDocsSet()) {
            if (id.equals(s.getID())) {
                deleteDoc(s);
                return;
            }
        }
        throw new SpreadsheetNotFoundException("No spreadsheet was found for the " + id.toString()
                + " identifier.");
    }

    public void deleteDoc(Spreadsheet doc) {
        doc.delete();
        removeDocs(doc);
    }

    public Spreadsheet getSpreadsheet(int id) {
        for (Spreadsheet doc : getDocsSet()) {
            if (doc.getID() == id)
                return doc;
        }
        return null;
    }
}
