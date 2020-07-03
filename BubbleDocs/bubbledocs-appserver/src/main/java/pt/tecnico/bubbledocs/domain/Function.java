package pt.tecnico.bubbledocs.domain;

import pt.tecnico.bubbledocs.exception.ShouldNotExecuteException;

public class Function extends Function_Base {

    public Function() {
        super();
    }

    public Integer getValue() {
        throw new ShouldNotExecuteException();
    }
}
