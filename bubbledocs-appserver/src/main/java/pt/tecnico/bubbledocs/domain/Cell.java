package pt.tecnico.bubbledocs.domain;

public class Cell extends Cell_Base {
    
    public Cell(int line, int column) {
        super();
        setLine(line);
        setColumn(column);
        setProtect(false);
    }
    
    public void delete(){
    	getContent().delete();
    	
    	deleteDomainObject();
    }
    
}
