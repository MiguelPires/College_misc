package pt.tecnico.bubbledocs.domain;

import java.util.ArrayList;
import java.util.List;
import pt.tecnico.bubbledocs.domain.Spreadsheet;

public class User extends User_Base {
    
    public User(String username, String name, String password) {
		super();
        setUsername(username);
        setName(name);
        setPassword(password);
    }
	
	User(){
	}
    
	public ArrayList<Spreadsheet> findCreatedDocs(String name){
		ArrayList<Spreadsheet> documents = new ArrayList<Spreadsheet>();
		for (Spreadsheet doc : this.getDocsCreatedSet())
			if(doc.getName().equals(name))
				documents.add(doc);
				
		return documents;
	}
}

