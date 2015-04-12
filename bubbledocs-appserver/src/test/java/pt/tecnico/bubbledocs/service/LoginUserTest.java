package pt.tecnico.bubbledocs.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;

import org.joda.time.LocalTime;
import org.joda.time.Seconds;
import org.junit.Test;

import pt.tecnico.bubbledocs.domain.ActiveUser;
import pt.tecnico.bubbledocs.domain.User;
import pt.tecnico.bubbledocs.exception.BubbleDocsException;
import pt.tecnico.bubbledocs.exception.LoginBubbleDocsException;
import pt.tecnico.bubbledocs.exception.RemoteInvocationException;
import pt.tecnico.bubbledocs.exception.UnknownBubbleDocsUserException;
import pt.tecnico.bubbledocs.exception.WrongPasswordException;
import pt.tecnico.bubbledocs.service.remote.IDRemoteServices;

// add needed import declarations

public class LoginUserTest extends BubbleDocsServiceTest {

    private String jp; // the token for user jp
    private String root; // the token for user root

    private static final String USERNAME = "jp";
    private static final String PASSWORD = "jp#";

    @Override
    public void populate4Test() throws BubbleDocsException {
        createUser(USERNAME, PASSWORD, "João Pereira");
    }

    // returns the time of the last access for the user with token userToken.
    // It must get this data from the session object of the application
    private LocalTime getLastAccessTimeInSession(String userToken) {
        ActiveUser user = getUserFromSession(userToken).getActiveUser();
        return user.getLastAccess().toLocalTime();
    }

    @Test
    public void success() {
        new MockUp<IDRemoteServices>() {
            @Mock
            public void loginUser(String username, String password) throws LoginBubbleDocsException,
                                                                            RemoteInvocationException {
                ;
            }
        };

        LoginUser service = new LoginUser(USERNAME, PASSWORD);
        service.execute();
        LocalTime currentTime = new LocalTime();

        String token = service.getUserToken();
        User user = getUserFromSession(token);
        assertEquals(USERNAME, user.getUsername());

        int difference = Seconds.secondsBetween(getLastAccessTimeInSession(token), currentTime)
                .getSeconds();
        assertTrue("Access time in session not correctly set", difference >= 0);
        assertTrue("diference in seconds greater than expected", difference < 2);
    }

    @Test
    public void successLoginTwice() {
        new MockUp<IDRemoteServices>() {
            @Mock
            public void loginUser(String username, String password) throws LoginBubbleDocsException,
                                                                            RemoteInvocationException {
                ;
            }
        };
        LoginUser service = new LoginUser(USERNAME, PASSWORD);
        service.execute();
        String token1 = service.getUserToken();

        service.execute();
        String token2 = service.getUserToken();

        User user = getUserFromSession(token1);
        assertNull(user);
        
        user = getUserFromSession(token2);
        assertEquals(USERNAME, user.getUsername());
    }
    
    @Test
    public void successNoServiceWithLocalCopy() {
        new MockUp<IDRemoteServices>() {
            @Mock
            public void loginUser(String username, String password) throws LoginBubbleDocsException,
                                                                            RemoteInvocationException {
                ;
            }
        };

        LoginUser service = new LoginUser(USERNAME, PASSWORD);
        service.execute();

        String token = service.getUserToken();
        User user = getUserFromSession(token);
        assertEquals(USERNAME, user.getUsername());
        
        new MockUp<IDRemoteServices>() {
            @Mock
            public void loginUser(String username, String password) throws LoginBubbleDocsException,
                                                                             RemoteInvocationException {
                throw new RemoteInvocationException("Service unavailable");
            }
        };
        
        service.execute();
        String token2 = service.getUserToken();
        user = getUserFromSession(token2);
        assertEquals(USERNAME, user.getUsername());
    }

    @Test(expected = LoginBubbleDocsException.class)
    public void loginUnknownUser() {
        new MockUp<IDRemoteServices>() {
            @Mock
            public void loginUser(String username, String password) throws LoginBubbleDocsException,
                                                                            RemoteInvocationException {
                throw new LoginBubbleDocsException("Unknown user");
            }
        };

        LoginUser service = new LoginUser("jp2", "jp");
        service.execute();
    }

    @Test(expected = LoginBubbleDocsException.class)
    public void loginUserWithinWrongPassword() {
        new MockUp<IDRemoteServices>() {
            @Mock
            public void loginUser(String username, String password) throws LoginBubbleDocsException,
                                                                             RemoteInvocationException {
                throw new LoginBubbleDocsException("Wrong password");
            }
        };

        LoginUser service = new LoginUser(USERNAME, "jp2");
        service.execute();
    }
    
    @Test(expected = RemoteInvocationException.class)
    public void NoServiceAndNoLocalCopy() {
        new MockUp<IDRemoteServices>() {
            @Mock
            public void loginUser(String username, String password) throws LoginBubbleDocsException,
                                                                             RemoteInvocationException {
                throw new RemoteInvocationException("Service unavailable");
            }
        };
    }
}
