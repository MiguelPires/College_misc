package pt.tecnico.bubbledocs.service;

import pt.ist.fenixframework.Atomic;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;

// add needed import declarations

public abstract class BubbleDocsService {

    @Atomic
    public final void execute() throws BubbleDocsException {
        dispatch();
    }

    protected abstract void dispatch() throws BubbleDocsException;
}
