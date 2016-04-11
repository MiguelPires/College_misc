package pt.ulisboa.tecnico.grupo11.ubiBikeServer;

import java.util.ArrayList;
import java.util.List;

public class User {
    private byte[] passwordHash;
    private int points;
    private List<String> paths;

    public User(byte[] passwordHash) throws InvalidArgumentsException {
        /*
         * if (username.length() < 5 || username == null) throw new InvalidArgumentsException("Invalid username '" + username +
         * "'"); else if (passwordHash.isEmpty() || passwordHash == null) throw new InvalidArgumentsException(
         * "Invalid password hash");
         */

        this.passwordHash = passwordHash;
        this.points = 0;
        this.paths = new ArrayList<String>();
        // default path, for testing
        paths.add("38.74924838,-9.20676827;38.76019789,-9.18283225");
        paths.add("27.74924838,-8.20676827;27.76019789,-8.18283225");
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

    public void setPoints(int points) {
        this.points = points;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public void addPath(String path) {
        paths.add(path);
    }

    public List<String> getPaths() {
        return paths;
    }
}
