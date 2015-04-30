package pt.tecnico.sdid;

import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TicketGranter {
	private String client;
	private String service;
	private String sessionKey;
	
	public TicketGranter(String client, String service) {
		this.client = client;
		this.service = service;		
	}
	
	public String getSessionKey () {
		return sessionKey;
	}
    private String generateEncodedKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey newKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(newKey.getEncoded());
    }
    
    private SecretKey getDecodedKey(String encodedKey) {        
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
	
    private byte[] cypherBytes(byte[] plain, SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(plain);
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
        client.appendChild(doc.createTextNode(this.client));
        server.appendChild(doc.createTextNode(this.service));
        
        Date d = new Date();
        beginsAt.appendChild(doc.createTextNode(d.toString()));
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.HOUR_OF_DAY, 2);
        Date newDate = c.getTime();
        expiresAt.appendChild(doc.createTextNode(newDate.toString()));
        
        // generate session key
        sessionKey = generateEncodedKey();
        key.appendChild(doc.createTextNode(sessionKey));
        
        // write XML document to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Result res = new StreamResult(bos);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(doc), res);
        byte[] docBytes = bos.toByteArray();
        
        SecretKey decodedKey = getDecodedKey(System.getProperty("key.server"));
        return cypherBytes(docBytes, decodedKey);
	}
}
