package pt.tecnico.bubbledocs.domain;

public class Argument extends Argument_Base {

    public Argument() {
        super();
    }

    public void delete() {
        setForbiddenBin1(null);
        setForbiddenBin2(null);
        super.delete();
    }
}
