package pt.tecnico.SDStore;

public class tag {
	private int seqNumber;
	private int clientID;
	
	public tag(int seqNumber, int clientID){
		this.seqNumber = seqNumber;
		this.clientID = clientID;
	}
	
	public tag(){
		seqNumber = -1;
		clientID = -1;
	}
	
	public void setID(int ID){
		clientID = ID;
	}
	
	public void setSeqNumber(int number){
		seqNumber = number;
	}
	
	public int getID(){
		return clientID;
	}
	
	public int getSeqNumber(){
		return seqNumber;
	}
	
	public boolean isGreater(tag newtag){
		if(seqNumber > newtag.getSeqNumber())
			return true;
		else if(seqNumber == newtag.getSeqNumber())
			if(clientID > newtag.getID())
				return true;
		
		return false;
	}
}
