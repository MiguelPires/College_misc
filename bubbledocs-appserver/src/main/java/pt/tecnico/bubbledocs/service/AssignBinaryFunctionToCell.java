package pt.tecnico.bubbledocs.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.tecnico.bubbledocs.domain.Addition;
import pt.tecnico.bubbledocs.domain.Argument;
import pt.tecnico.bubbledocs.domain.Cell;
import pt.tecnico.bubbledocs.domain.Division;
import pt.tecnico.bubbledocs.domain.Literal;
import pt.tecnico.bubbledocs.domain.Multiplication;
import pt.tecnico.bubbledocs.domain.Reference;
import pt.tecnico.bubbledocs.domain.Spreadsheet;
import pt.tecnico.bubbledocs.domain.Subtraction;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.UnauthorizedOperationException;

public class AssignBinaryFunctionToCell extends CheckLogin {
	private int docId;
    private int cellRow;
    private int cellColumn;

    private String result;
    private String cellId;
    private String function;

    public String getResult() {
        return this.result;
    }

    public AssignBinaryFunctionToCell(String cellId, String function, int docId, String userToken) {
    	this.userToken = userToken;
        this.docId = docId;
        this.cellId = cellId;
        this.function = function;
    }
    
    public Argument getArgument(String arg, Spreadsheet doc){
    	Argument argument;
    	Pattern patternLit = Pattern.compile("([0-9]+)");
    	Pattern patternRef = Pattern.compile("([0-9]+;[0-9]+)");
        Matcher matcherLit = patternLit.matcher(arg);
        Matcher matcherRef = patternRef.matcher(arg);

        if (matcherRef.find()){
        	String[] argCoords = matcherRef.group(1).split(";");
            int argRow = Integer.parseInt(argCoords[0]);
            int argColumn = Integer.parseInt(argCoords[1]);
            
            Cell reference = doc.getCell(argRow, argColumn);
            Reference ref = new Reference(reference);
        	argument = ref;
        } else if(matcherLit.find()) {
        	argument = new Literal(Integer.parseInt(arg));
        } else {
        	throw new UnauthorizedOperationException("Wrong function " + function + ".");
        }
        return argument;
        
    }

    @Override
    protected void dispatch() throws BubbleDocsException {
        super.dispatch();

        Spreadsheet doc = getBubbledocs().getSpreadsheet(docId);
        
        String func, content1, content2;
        Argument arg1, arg2;

        if (doc.getCreator().equals(user) || doc.isWriter(user.getUsername())) {
            try {
                String[] cellCoordinates = cellId.split(";");
                cellRow = Integer.parseInt(cellCoordinates[0]);
                cellColumn = Integer.parseInt(cellCoordinates[1]);
                
                Pattern pattern = Pattern.compile("([A-Z]{3})\\(([0-9]+|([0-9]+;[0-9]+)),([0-9]+|([0-9]+;[0-9]+))\\)");
                Matcher matcher = pattern.matcher(function);
                if(matcher.find()){
                	func = matcher.group(1);
                	content1 = matcher.group(2);
                	content2 = matcher.group(4);
                } else {
                	throw new UnauthorizedOperationException("Wrong function " + function + ".");
                }
                
                arg1 = getArgument(content1, doc);
                arg2 = getArgument(content2, doc);
                
                if (func.equals("ADD")) {
                	Addition add = new Addition(arg1, arg2);
                	doc.addCellContent(cellRow, cellColumn, add);
                } else if (func.equals("DIV")) {
                	Division div = new Division(arg1, arg2);
                	doc.addCellContent(cellRow, cellColumn, div);
                } else if (func.equals("MUL")) {
                	Multiplication mul = new Multiplication(arg1, arg2);
                	doc.addCellContent(cellRow, cellColumn, mul);
                } else if (func.equals("SUB")) {
                	Subtraction sub = new Subtraction(arg1, arg2);
                	doc.addCellContent(cellRow, cellColumn, sub);
                } else {
                	throw new UnauthorizedOperationException("Wrong function " + function + ".");
                }
   
            } catch (Exception e) {
                throw new UnauthorizedOperationException("Wrong function " + function + ".");
            }

            Integer res = doc.getCell(cellRow, cellColumn).getContent().getValue();

            if (res == null)
                this.result = null;
            else 
                this.result = res.toString();
            
        } else
            throw new UnauthorizedOperationException();
    }
}
