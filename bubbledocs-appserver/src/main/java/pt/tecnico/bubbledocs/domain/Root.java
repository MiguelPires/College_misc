package pt.tecnico.bubbledocs.domain;

public class Root extends Root_Base {

    public Root(Bubbledocs bubble) {
        super();
        setUsername("root");
        setName("Super User");
        setPassword("root");
        setEmail("root@root");
        setApplication(bubble);
    }

    @Override
    public boolean isRoot() {
        return true;
    }
}
