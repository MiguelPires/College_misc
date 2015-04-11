package pt.tecnico.SDStore;

import java.util.ArrayList;

public class userDirectory {

	private ArrayList <document> storedDocs;
	private String user;
	
	public userDirectory(String user){
		storedDocs = new ArrayList<document>();
		this.user = user;
	}
	
	public void addDoc(String id){
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
