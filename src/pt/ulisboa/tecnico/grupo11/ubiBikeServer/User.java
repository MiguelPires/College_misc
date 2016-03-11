package pt.ulisboa.tecnico.grupo11.ubiBikeServer;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String passwordHash;
    private int points;
    private List<Path> paths;

    public User(String username, String passwordHash) throws InvalidArgumentsException {

       /* if (username.length() < 5 || username == null)
            throw new InvalidArgumentsException("Invalid username '" + username + "'");
        else if (passwordHash.isEmpty() || passwordHash == null)
            throw new InvalidArgumentsException("Invalid password hash");*/

        this.username = username;
        this.passwordHash = passwordHash;
        this.points = 0;
        this.paths = new ArrayList<Path>();
    }

    public void addPoints(int newPoints) throws InvalidArgumentsException {
        if (newPoints < 0)
            points += newPoints;
        else
            throw new InvalidArgumentsException("Can't add negative points.");
    }

    public void removePoints(int newPoints) throws InvalidArgumentsException {
        if (newPoints < 0)
            points += newPoints;
        else
            throw new InvalidArgumentsException("Can't remove negative points.");
    }

    public int getPoints() {
        return points;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
