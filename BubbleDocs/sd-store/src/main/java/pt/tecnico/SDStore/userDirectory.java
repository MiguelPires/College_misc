package pt.tecnico.SDStore;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.sdis.store.ws.*;

public class userDirectory {

	private ArrayList <document> storedDocs;
	private String user;
	//private CapacityExceeded capacity;

	public userDirectory(String user){
		storedDocs = new ArrayList<document>();
		//capacity = new CapacityExceeded();
		//capacity.setAllowedCapacity(10*1024);
		//capacity.setCurrentSize(0);
		this.user = user;
	}
	
	public void addDoc(String id) throws DocAlreadyExists_Exception{
		if(docExists(id)) {
			DocAlreadyExists faultinfo = new DocAlreadyExists();
			faultinfo.setDocId(id);
			throw new DocAlreadyExists_Exception("A document already exists with the same id", faultinfo);
		}
		
		document doc = new document(id);
		storedDocs.add(doc);
	}
	
	public boolean docExists(String id){
		for(document doc : storedDocs)
			if(doc.getId().equals(id))
				return true;
		
		return false;
	}
	
	// checks if user folder is full (10*1024 bytes)
	/*public boolean isFull(){
		if((capacity.getAllowedCapacity() - capacity.getCurrentSize())<0)
			return true;
		return false;
	}*/

	
	public String getUser(){
		return user;
	}

	public void storeContent(String docId, byte[] content, String[] tag) throws CapacityExceeded_Exception, DocDoesNotExist_Exception{
		//if(isFull())
			//throw new CapacityExceeded_Exception("Repository storage capacity of the user is exceeded", capacity);

		for(document doc: storedDocs)
			if(doc.getId().equals(docId)){
				tag newTag = new tag(Integer.parseInt(tag[0]), Integer.parseInt(tag[1]));
				if(newTag.isGreater(doc.getTag())){ // tests if tag is greater than old tag, and only updates if soo
					doc.setContent(content); //updateDoc(doc, content);
					doc.setTagID(Integer.parseInt(tag[1]));
					doc.setTagNumber(Integer.parseInt(tag[0]));
				}
				return;
			}

		DocDoesNotExist doc = new DocDoesNotExist();
		doc.setDocId(docId);
		throw new DocDoesNotExist_Exception("Document does not exist", doc);
	}
	
	//makes verifications to know if it's possible to set the content without overflowing the user's capacity
	/*public void updateDoc(document doc, byte[] content) throws CapacityExceeded_Exception{
		byte[] current = doc.getContent();
		int oldSize=capacity.getCurrentSize();
		if(current != null)
			capacity.setCurrentSize(oldSize + content.length - current.length);
		else
			capacity.setCurrentSize(oldSize + content.length);
		
		if(isFull()){
			capacity.setCurrentSize(oldSize);
			throw new CapacityExceeded_Exception("Repository storage capacity of the user is exceeded", capacity);
		}
		else
			doc.setContent(content);
	}*/

	public document loadDoc(String docId) throws DocDoesNotExist_Exception{
		for(document doc: storedDocs)
			if(doc.getId().equals(docId))
				return doc;

		DocDoesNotExist doc = new DocDoesNotExist();
		doc.setDocId(docId);
		throw new DocDoesNotExist_Exception("Document does not exist", doc);
	}

	public List<String> getDocsNames(){
		ArrayList<String> docs = new ArrayList<String>();
		for(document doc: storedDocs)
			docs.add(doc.getId());
		return docs;
	}
	
	/*public CapacityExceeded getCapacity(){
		return capacity;
	}*/
}
