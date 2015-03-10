package pt.tecnico.bubbledocs.domain;

public class Spreadsheet extends Spreadsheet_Base {

	public Spreadsheet() {
		super();
	}

	public void delete(){
		
    	for(Cell c : getCellsSet()){
    		c.delete();
    		removeCells(c);
    	}
    	
    	getCreator().removeCreatedDocs(this);
    	
    	for(User u : getWritersSet()){
    		u.removeWritableDocs(this);
    		removeWriters(u);
    	}
    	
    	for(User u : getReadersSet()){
    		u.removeReadableDocs(this);
    		removeReaders(u);
    	}
    	 
    	deleteDomainObject();
    }
}
