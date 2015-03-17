package pt.tecnico.bubbledocs.domain;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class ActiveUser extends ActiveUser_Base {
    
    public ActiveUser(User user, String token, Session session) {
        super();
        setLoggedUser(user);
        setToken(token);
        setSession(session);
        setLastAccess(new DateTime(DateTimeZone.getDefault()));
    }
    
    public void delete()
    {
        getSession().removeActiveUsers(this);
        getLoggedUser().setActiveUser(null);
        setLoggedUser(null);
        setSession(null);
        deleteDomainObject();
    }
}
