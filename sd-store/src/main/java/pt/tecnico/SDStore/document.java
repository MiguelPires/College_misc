package pt.tecnico.SDStore;

import java.util.ArrayList;

public class document {

	private String id;
	private byte[] content;
	
	public document (String docId){
		id=docId;
		content=null;
	}
	
	public document (String docId, byte[] content){
		id=docId;
		this.content=content;
	}
	
	public void setContent(byte[] content){
		this.content=content;
	}
	
	public byte[] getContent(){
		return content;
	}
	
	public String getId(){
		return id;
	}
}
