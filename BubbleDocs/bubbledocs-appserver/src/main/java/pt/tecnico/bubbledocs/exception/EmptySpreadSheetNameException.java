package pt.tecnico.bubbledocs.exception;

public class EmptySpreadSheetNameException extends BubbleDocsException {
    public EmptySpreadSheetNameException() {
        super();
    }

    public EmptySpreadSheetNameException(String message) {
        super(message);
    }
}
