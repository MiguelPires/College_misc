package pt.tecnico.bubbledocs;

import java.util.ArrayList;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.jdom2.Element;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.TransactionManager;
import pt.tecnico.bubbledocs.domain.Bubbledocs;
import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;
import pt.tecnico.bubbledocs.exception.UnknownBubbleDocsUserException;

public class BubbleApplication {

    private static Bubbledocs bubbleapp;

    public static void main(String[] args) throws ShouldNotExecuteException,
                                          UnauthorizedOperationException {

        System.out.println("+--------------------------------------------+");
        System.out.println("+   Welcome to the Bubbledocs application!   +");
        System.out.println("+--------------------------------------------+");


        TransactionManager tm = FenixFramework.getTransactionManager();
        boolean committed = false;

        try {
            tm.begin();

            bubbleapp = Bubbledocs.getInstance();
            setupIfNeed(bubbleapp);

            tm.commit();
            committed = true;
        } catch (SystemException | NotSupportedException | RollbackException
                | HeuristicMixedException | HeuristicRollbackException ex) {
            System.err.println("Error in execution of transaction: " + ex);
        } finally {
            if (!committed) {
                try {
                    tm.rollback();
                } catch (SystemException ex) {
                    System.err.println("Error in roll back of transaction: " + ex);
                }
            }
        }

        printUsers();
        printSpreadsheets("pfs");
        printSpreadsheets("ras");

        //aceder as spreadsheets, converter e escrever o resultado
        ArrayList<org.jdom2.Document> docList = exportUserDocs("pfs");

        //remover a spreadsheet do pf
        deleteSpreadsheet("pfs", "Notas ES");

        printSpreadsheetsID("pfs");

        //importar spreadsheet        
        for (org.jdom2.Document doc : docList) {
            importUserDocs("pfs", doc);
        }

        //aceder as spreadsheets, converter e escrever o resultado
        docList = exportUserDocs("pfs");
    }

    // setup the initial state if Bubbledocs is empty
    @Atomic
    private static void setupIfNeed(Bubbledocs bubbleapp) {
        for (User user : bubbleapp.getUsersSet()) {
            if (user.getUsername().equals("root")) {
                continue;
            } else {
                return;
            }
        }
        SetupDomain.populateDomain();
    }

    @Atomic
    private static ArrayList<org.jdom2.Document> exportUserDocs(String userName)
                                                                                throws ShouldNotExecuteException {
        ArrayList<org.jdom2.Document> docList = new ArrayList<org.jdom2.Document>();

        try {
            User u = bubbleapp.getUser(userName);

            org.jdom2.Document doc;

            for (Spreadsheet s : u.getCreatedDocsSet()) {

                System.out.println(s.getName());
                doc = bubbleapp.exportToXML(s);
                docList.add(doc);
                System.out.println(bubbleapp.getDomainInXML(doc));
            }
        } catch (UnknownBubbleDocsUserException e) {
            System.out.println(e.getMessage());
        }
        return docList;
    }

    @Atomic
    public static void importUserDocs(String userName, org.jdom2.Document jdomDoc)
                                                                                  throws UnauthorizedOperationException {
        Element doc = jdomDoc.getRootElement();
        Element creatorElement = doc.getChild("creator");
        Element userElement = creatorElement.getChild("user");

        String xmlUsername = userElement.getAttribute("username").getValue();

        if (xmlUsername.equals(userName))
            importFromXML(jdomDoc);
        else
            throw new UnauthorizedOperationException("The exported document doesn't belong to "
                    + userName);
    }

    private static void importFromXML(org.jdom2.Document jdomDoc) {
        bubbleapp.importFromXML(jdomDoc.getRootElement());
    }

    @Atomic
    public static void deleteSpreadsheet(String username, String docName) {
        User user = bubbleapp.getUser(username);
        user.getSpreadsheet(docName).delete();
    }

    @Atomic
    public static void printUsers() {
        if (bubbleapp.getUsersSet().isEmpty()) {
            System.out.println("No users were found.");
        }
        for (User user : bubbleapp.getUsersSet()) {
            System.out.println("BubbleDocs User: " + user.getUsername());
            System.out.println("\t Name: " + user.getName());
            System.out.println("\t Password: " + user.getPassword());
        }
    }

    @Atomic
    public static void printSpreadsheets(String username) {
        User user;
        try {
            user = bubbleapp.getUser(username);

            if (user.getCreatedDocsSet().isEmpty()) {
                System.out.println("No spreadsheets were created by: " + user.getUsername() + ".");
            } else {
                System.out.println("Documents created by: " + user.getUsername());

                for (Spreadsheet spreadsheet : user.getCreatedDocsSet()) {
                    System.out.println("\t - " + spreadsheet.getName());
                }
            }

        } catch (UnknownBubbleDocsUserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Atomic
    public static void printSpreadsheetsID(String username) {
        User user;
        try {
            user = bubbleapp.getUser(username);

            if (user.getCreatedDocsSet().isEmpty()) {
                System.out.println("No spreadsheets were created by: " + user.getUsername() + ".");
            } else {
                System.out.println("Documents created by: " + user.getUsername());

                for (Spreadsheet spreadsheet : user.getCreatedDocsSet()) {
                    System.out.println("\t - " + spreadsheet.getName() + ", id = "
                            + spreadsheet.getID());
                }
            }
        } catch (UnknownBubbleDocsUserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
