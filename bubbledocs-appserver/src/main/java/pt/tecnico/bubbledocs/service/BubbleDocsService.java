package pt.tecnico.bubbledocs.service;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;
import pt.tecnico.bubbledocs.domain.ActiveUser;
import pt.tecnico.bubbledocs.domain.Bubbledocs;
import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;
import pt.tecnico.bubbledocs.exception.UnknownBubbleDocsUserException;
import pt.tecnico.bubbledocs.exception.UserNotInSessionException;

public abstract class BubbleDocsService {

    @Atomic
    public final void execute() throws BubbleDocsException {
        dispatch();
    }

    protected abstract void dispatch() throws BubbleDocsException;

    public static Bubbledocs getBubbledocs() {
        return FenixFramework.getDomainRoot().getBubbledocs();
    }

    public static User createUser(String username, String name, String password) throws BubbleDocsException {
        return Bubbledocs.getInstance().createUser(username, name, password);
    }

    public static User getUser(String username) throws UnknownBubbleDocsUserException {
        return getBubbledocs().getUser(username);
    }

    public static User getUserByToken(String token) throws UserNotInSessionException {
        return getBubbledocs().getUserByToken(token);
    }

    public static ActiveUser getActiveUserByUsername(String username) throws UserNotInSessionException {
        return getBubbledocs().getActiveUserByUsername(username);
    }

    public static String addUserToSession(String username) throws UnknownBubbleDocsUserException {
        return getBubbledocs().addUserToSession(username);
    }

    public static void removeUserByToken(String token) throws UserNotInSessionException {
        getBubbledocs().removeUserByToken(token);
    }

    public static Spreadsheet createSpreadsheet(User user, String name, int row, int column) {

        return getBubbledocs().createSpreadsheet(user, name, row, column);
    }

    public static Spreadsheet getSpreadsheet(int id) {
        return getBubbledocs().getSpreadsheet(id);
    }
    
    public static Spreadsheet getSpreadsheet(String name) {
        return getBubbledocs().getSpreadsheet(name);
    }

    public static org.jdom2.Document exportToXML(int id) throws ShouldNotExecuteException {
        return getBubbledocs().exportToXML(id);
    }
    
    public static Spreadsheet importFromXML (org.jdom2.Document xml)
    {
        return getBubbledocs().importFromXML(xml.getRootElement());
    }

    public static boolean isLoggedIn(User user) {
        try {
            getActiveUserByUsername(user.getUsername());
            return true;
        } catch (UserNotInSessionException e) {
            return false;
        }
    }
}
