package pt.tecnico.bubbledocs.domain;

import java.util.ArrayList;
import java.util.Random;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import pt.ist.fenixframework.FenixFramework;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.DuplicateEmailException;
import pt.tecnico.bubbledocs.exception.DuplicateUsernameException;
import pt.tecnico.bubbledocs.exception.EmptyUsernameException;
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

    public User createUser(String username, String name, String email) throws BubbleDocsException {
        User user = new User(username, name, email);
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

        }

        try {
            getUserByEmail(user.getEmail());
            throw new DuplicateEmailException();
        } catch (UnknownBubbleDocsUserException e) {

        }

        super.addUsers(user);
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

    public User getUserByEmail(String email) throws UnknownBubbleDocsUserException {
        for (User user : getUsersSet()) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        throw new UnknownBubbleDocsUserException("User with email '" + email + "' not found.");
    }

    public User getUserByToken(String token) throws UserNotInSessionException {
        return getSession().getUserByToken(token);
    }

    public ActiveUser getActiveUserByUsername(String username) throws UserNotInSessionException {
        return getSession().getActiveUserByUsername(username);
    }

    public Spreadsheet importFromXML(Element spreadsheetElement) {
        Spreadsheet spread = new Spreadsheet();
        spread.importFromXML(spreadsheetElement, this);

        return spread;
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
        return user.createSpreadsheet(name, rows, columns);
    }

    public ArrayList<Spreadsheet> getCreatedDocsByUser(User user, String name) {
        return user.getCreatedSpreadsheets(name);
    }

    public void deleteSpreadsheet(Integer id) throws SpreadsheetNotFoundException {
        getSpreadsheet(id).delete();
    }

    public void deleteDoc(Spreadsheet doc) {
        doc.delete();
    }

    public Spreadsheet getSpreadsheet(int id) {
        for (User user : getUsersSet()) {
            try {
                return user.getSpreadsheet(id);
            } catch (SpreadsheetNotFoundException e) {
                ;
            }
        }
        throw new SpreadsheetNotFoundException("Spreadsheet " + id + " not found"); // a spreadsheet nao pertence a este user
    }

    // por indicação do professor, não serão inseridas Spreadsheets com o mesmo nome
    public Spreadsheet getSpreadsheet(String name) {
        for (User user : getUsersSet()) {
            try {
                return user.getSpreadsheet(name);
            } catch (SpreadsheetNotFoundException e) {
                ; // a spreadsheet nao pertence a este user
            }
        }
        return null;
    }

    public String[][] getSpreadsheetContent(int docId) {
        Spreadsheet ss = getSpreadsheet(docId);
        return ss.getSpreadsheetContent();
    }

    public void renewPassword(User user) {
        user.invalidatePassword();
    }

    public void validatePassword(User user) {
        user.validatePassword();
    }

    public int getNewID() {
        int id = getLastID() + 1;
        setLastID(id);
        return id;
    }
}
