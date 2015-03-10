package pt.tecnico.bubbledocs.domain;

import java.util.ArrayList;
import pt.tecnico.bubbledocs.exception.SpreadsheetNotFoundException;

public class Bubbledocs extends Bubbledocs_Base {
    
    public Bubbledocs() {
        super();
    }
    
	public ArrayList<Spreadsheet> findCreatedDocsByUser(User user, String name){
		return user.findCreatedDocs(name);
	}
	
	public void deleteDoc(Integer id) throws SpreadsheetNotFoundException
	{
	    for (Spreadsheet s: getDocsSet())
	    {
	        if (id.equals(s.getID()))
	        {
	            deleteDoc(s);
	            return;
	        }
	    }
	    throw new SpreadsheetNotFoundException("Not spreadsheet found for the "+id.toString()+" identifier.");	    
	}
	
	public void deleteDoc(Spreadsheet doc)
	{
	    doc.delete();
	    removeDocs(doc);
	}
}
