package pt.tecnico.SDStore;

import java.util.ArrayList;

import pt.ulisboa.tecnico.sdis.store.ws.*;

public class userDirectory {

	private ArrayList <document> storedDocs;
	private String user;
	private CapacityExceeded capacity;

	public userDirectory(String user){
		storedDocs = new ArrayList<document>();
		capacity = new CapacityExceeded();
		capacity.setAllowedCapacity(10*1024);
		capacity.setCurrentSize(0);
		this.user = user;
	}
	
	public void addDoc(String id) throws DocAlreadyExists_Exception{
		if(docExists(id)) {
			DocAlreadyExists faultinfo = new DocAlreadyExists();
			faultinfo.setDocId(id);
			throw new DocAlreadyExists_Exception("A document already exists with the same id", faultinfo);
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
		if((capacity.getAllowedCapacity() - capacity.getCurrentSize())<0)
			return true;
		return false;
	}

	public boolean isFull(byte[] content){
		if((capacity.getAllowedCapacity() - (capacity.getCurrentSize() + content.length))<0)
			return true;
		return false;
	}
	
	public String getUser(){
		return user;
	}

	public void storeContent(String docId, byte[] content) throws CapacityExceeded_Exception, DocDoesNotExist_Exception{
		if(isFull(content))
			throw new CapacityExceeded_Exception("Repository storage capacity of the user is exceeded", capacity);

		for(document doc: storedDocs)
			if(doc.getId().equals(docId)){
				doc.setContent(content);
				return;
			}

		DocDoesNotExist doc = new DocDoesNotExist();
		doc.setDocId(docId);
		throw new DocDoesNotExist_Exception("Document does not exist", doc);
	}

	public byte[] loadContent(String docId) throws DocDoesNotExist_Exception{
		for(document doc: storedDocs)
			if(doc.getId().equals(docId))
				return doc.getContent();

		DocDoesNotExist doc = new DocDoesNotExist();
		doc.setDocId(docId);
		throw new DocDoesNotExist_Exception("Document does not exist", doc);
	}

	public ArrayList<document> getDocs(){
		return storedDocs;
	}
}
