package pt.tecnico.sdid;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pt.tecnico.CryptoHelper;

public class TicketGranter {
	private String clientName;
	private String serviceName;
	private String sessionKey;
	private String serverKey;
	private CryptoHelper crypto;
	
    private String getServerKey() {
        return serverKey;
    }
    public String getSessionKey () {
        return sessionKey;
    }
    
	public TicketGranter(String clientName, String serviceName, String serverKey) {
		this.clientName = clientName;
		this.serviceName = serviceName;		
		this.serverKey = serverKey;
		this.crypto = new CryptoHelper("AES", "CBC", "PKCS5Padding");
	}

	public byte[] grant() throws Exception {
		// create XML document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // create root node 
        Element ticket = doc.createElement("Ticket");
        doc.appendChild(ticket);
        
        // append children nodes
        Element client = doc.createElement("Client");
        ticket.appendChild(client);
        Element server = doc.createElement("Server");
        ticket.appendChild(server);
        Element beginsAt = doc.createElement("BeginsAt");
        ticket.appendChild(beginsAt);
        Element expiresAt = doc.createElement("ExpiresAt");
        ticket.appendChild(expiresAt);
        Element key = doc.createElement("SessionKey");
        ticket.appendChild(key);
        
        // append text to children nodes
        client.appendChild(doc.createTextNode(this.clientName));
        server.appendChild(doc.createTextNode(this.serviceName));
        
        Date d = new Date();
        beginsAt.appendChild(doc.createTextNode(d.toString()));
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.HOUR_OF_DAY, 2);
        Date newDate = c.getTime();
        expiresAt.appendChild(doc.createTextNode(newDate.toString()));
        
        // generate session key
        sessionKey = crypto.encodeKey(crypto.generateKey());
        key.appendChild(doc.createTextNode(getSessionKey()));
        
        // write XML document to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Result res = new StreamResult(bos);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        transformer.transform(new DOMSource(doc), res);
        byte[] docBytes = bos.toByteArray();
        
        SecretKey decodedKey = crypto.decodeKey(getServerKey());
        return crypto.cipherBytes(docBytes, decodedKey);
	}
}
