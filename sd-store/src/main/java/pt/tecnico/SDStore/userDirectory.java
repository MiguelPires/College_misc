package pt.tecnico.SDStore;

import java.util.ArrayList;

import pt.ulisboa.tecnico.sdis.store.ws.DocAlreadyExists;
import pt.ulisboa.tecnico.sdis.store.ws.DocAlreadyExists_Exception;

public class userDirectory {

	private ArrayList <document> storedDocs;
	private String user;
	
	public userDirectory(String user){
		storedDocs = new ArrayList<document>();
		this.user = user;
	}
	
	public void addDoc(String id) throws DocAlreadyExists_Exception{
		if(docExists(id)) {
			DocAlreadyExists faultinfo = new DocAlreadyExists();
			faultinfo.setDocId(id);
			throw new DocAlreadyExists_Exception("Document ID already exists", faultinfo);
		}
		
		storedDocs.add(new document(id));
	}
	
	public boolean docExists(String id){
		for(document doc : storedDocs)
			if(doc.getId().equals(id))
				return true;
		
		return false;
	}
	
	// checks if user folder is full (10*1024 bytes)
	public boolean isFull(){
		int totalLength=0;
		for(document doc : storedDocs){
			totalLength += doc.getContent().length;
			if(totalLength>(10*1024))
				return true;
		}
		return false;
	}
	
	public String getUser(){
		return user;
	}
}
