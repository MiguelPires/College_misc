package pt.tecnico.bubbledocs.domain;

import pt.tecnico.bubbledocs.exception.ImportDocumentException;

import org.jdom2.Element;
import org.jdom2.DataConversionException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class Spreadsheet extends Spreadsheet_Base {

	public Spreadsheet()
	{
		super();
	}
	
	public Spreadsheet(Integer id, String name, Integer lines, Integer columns, User creator) {
        super();
        
        setID(id);
        setName(name);
        setLines(lines);
        setColumns(columns);
        setCreator(creator);
        
	    DateTime date = new DateTime(DateTimeZone.getDefault());
	    setCreatedAt(date);
	    setModifiedAt(date);
	    
	}

	public Cell getCell (Integer line, Integer column)
	{
	    //adicionar verificações
	    for (Cell cell: getCellsSet())
        {
            if (cell.getLine().equals(line) && cell.getColumn().equals(column))
            {
                return cell;
            }
        }
	    
	    Cell cell = new Cell (line, column); 
	    addCells(cell);
	    return cell;
	}
	public void addCellContent (Integer line, Integer column, Content content)
	{
	  //adicionar verificações 
	    for (Cell cell: getCellsSet())
	    {
	        if (cell.getLine().equals(line) && cell.getColumn().equals(column))
	        {
	            cell.setContent(content);
	            return;
	        }
	    }
	    addCells(new Cell (line, column, content));
	}
	
	public void deleteCellContent (Integer line, Integer column)
	{
	  //adicionar verificações
	    for (Cell cell: getCellsSet())
        {
            if (cell.getLine().equals(line) && cell.getColumn().equals(column))
            {
                cell.delete();
                removeCells (cell);
                return;
            }
        }
	}
	
	public void delete(){
		
    	for(Cell c : getCellsSet()){
    		removeCells(c);
            c.delete();
    	}
    	
    	
    	getCreator().removeCreatedDocs(this);
    	setCreator(null);
    	
    	for(User u : getWritersSet()){
    		u.removeWritableDocs(this);
    		removeWriters(u);
    	}
    	
    	for(User u : getReadersSet()){
    		u.removeReadableDocs(this);
    		removeReaders(u);
    	}
    	 
    	setBubbleApp(null);
    	deleteDomainObject();
    }
	
	public Element exportToXML() {
		Element element = new Element("spreadsheet");
		element.setAttribute("ID",  Integer.toString(getID()));
		element.setAttribute("name", getName());
		
		element.setAttribute("createdAt", ""+getCreatedAt());
		element.setAttribute("modifiedAt", ""+getModifiedAt());

		element.setAttribute("line",  Integer.toString(getLines()));
		element.setAttribute("column",  Integer.toString(getColumns()));
		
		Element cellElement = new Element("cells");
		element.addContent(cellElement);

		for (Cell c : getCellsSet()) {
		    cellElement.addContent(c.exportToXML());
		}
		
		Element creatorElement = new Element("creator");
		element.addContent(creatorElement);
		creatorElement.addContent((getCreator()).exportToXML());
		
		return element;
	}
	
	public void importFromXML(Element spreadsheetElement) {
		
	    
       try {
            setID(spreadsheetElement.getAttribute("ID").getIntValue());
            setLines(spreadsheetElement.getAttribute("line").getIntValue());
            setColumns(spreadsheetElement.getAttribute("column").getIntValue());
        } catch (DataConversionException e) { 
            throw new ImportDocumentException();
        }
      
		
		String createTimeString = spreadsheetElement.getAttribute("createdAt").getValue();
		DateTime created = new DateTime(createTimeString, DateTimeZone.getDefault());
		
		String modifiedTimeString = spreadsheetElement.getAttribute("modifiedAt").getValue();
		DateTime modified= new DateTime(modifiedTimeString, DateTimeZone.getDefault());

		setName(spreadsheetElement.getAttribute("name").getValue());
		setCreatedAt(created);
		setModifiedAt(modified);
		
		Element cells = spreadsheetElement.getChild("cells");
	
		for (Element cell: cells.getChildren("cell"))
		{
		    Cell c = new Cell();
		    c.importFromXML(cell);
		    addCells(c);
		}
		
		Element crt = spreadsheetElement.getChild("creator");
		
		
		User u = new User();
		u.importFromXML(crt.getChild("users"));
		u.addCreatedDocs(this);
		setCreator(u);
		
		
	}

}

