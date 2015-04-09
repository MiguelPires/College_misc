package pt.tecnico.sdid;

public class User {

    private String userId;
    private String email;
    private String password;
    
    public User(String userId, String email, String password) {
        setEmail(email);
        setUserId(userId);
        setPassword(password);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
