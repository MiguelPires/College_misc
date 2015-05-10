package pt.tecnico;

import java.util.List;

import javax.xml.transform.TransformerFactoryConfigurationError;

import pt.ulisboa.tecnico.sdis.store.ws.DocUserPair;

public class ClientMain {
    protected static final String UDDI_URL = "http://localhost:8081";
    protected static final String ID_NAME = "SD-ID";
    protected static final String STORE_NAME = "SD-STORE";
    public static final String CLIENT_KEY = "bH7OZp6X11DNSrBr2MBt6g==";
    protected static final String CLIENT_NAME = "BubbleDocs";

    public static void main(String[] args) throws TransformerFactoryConfigurationError, Exception {
        Client c = new Client();
        System.out.println("\n ******* CLIENT *******");

        System.out.println("Press enter to proceed");
        System.in.read();
        
        c.requestAuthentication("alice", "Aaa1".getBytes());
        
        System.out.println("Press enter to proceed");
        System.in.read();
        
        DocUserPair du = new DocUserPair();
        du.setUserId("alice");
        du.setDocumentId("Doc1");
        c.storeClient.createDoc(du);

        du.setUserId("alice");
        du.setDocumentId("Doc2");
        c.storeClient.createDoc(du);
        
        List<String> docs = c.storeClient.listDocs("alice");

        System.out.println("Alice's documents: ");
        for (String doc : docs) {
            System.out.println(doc);
        }
        
        System.out.println("Press enter to proceed");
        System.in.read();
        
        c.requestAuthentication("bruno", "Bbb2".getBytes());

        du.setUserId("bruno");
        du.setDocumentId("Doc3");
        c.storeClient.createDoc(du);
        
        docs = c.storeClient.listDocs("bruno");
        System.out.println("Bruno's documents: ");
        for (String doc : docs) {
            System.out.println(doc);
        }
    }
}
