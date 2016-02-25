package sec.blockfs.blockserver;

public class DataIntegrityFailureException extends Exception {
  public DataIntegrityFailureException () {
    super();
  }
  
  public DataIntegrityFailureException (String message) {
    super(message);
  }
}
