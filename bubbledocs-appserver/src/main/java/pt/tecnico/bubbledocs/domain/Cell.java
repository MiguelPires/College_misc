package pt.tecnico.bubbledocs.domain;

public class Cell extends Cell_Base {
    
    public Cell() {
        super();
    }
    
    public void delete(){
    	getContent().delete();
    	
    	deleteDomainObject();
    }
    
}
