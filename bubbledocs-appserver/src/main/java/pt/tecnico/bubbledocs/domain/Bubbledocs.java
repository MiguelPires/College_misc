package pt.tecnico.bubbledocs.domain;

import java.util.ArrayList;
import java.util.List;

public class Bubbledocs extends Bubbledocs_Base {
    
    public Bubbledocs() {
        super();
    }
    
	public ArrayList<Spreadsheet> findCreatedDocsByUser(User user, String name){
		return user.findCreatedDocs(name);
	}
}
