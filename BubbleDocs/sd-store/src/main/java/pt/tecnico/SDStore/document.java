package pt.tecnico.SDStore;

public class document {

	private String id;
	private byte[] content;
	private tag tag;
	
	public document (String docId){
		id=docId;
		content=null;
		tag = new tag();
	}
	
	public document (String docId, byte[] content){
		id=docId;
		this.content=content;
		tag = new tag();
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
	
	public void setTagID(int newID){
		tag.setID(newID);
	}
	
	public void setTagNumber(int number){
		tag.setSeqNumber(number);
	}
	
	public int getTagID(){
		return tag.getID();
	}
	
	public int getSeqNumber(){
		return tag.getSeqNumber();
	}
	
	public tag getTag(){
		return tag;
	}
}
