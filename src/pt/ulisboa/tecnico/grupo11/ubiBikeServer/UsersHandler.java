package pt.ulisboa.tecnico.grupo11.ubiBikeServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class UsersHandler implements HttpHandler {
    private static final int MAX_DIGEST_SIZE = 1024;
    private Hashtable<String, User> users;

    public UsersHandler() throws UnsupportedEncodingException, InvalidArgumentsException {
        users = new Hashtable<String, User>();
        User user = new User("randomtext".getBytes("UTF-8"));
        users.put("miguel", user);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("URI: " + exchange.getRequestURI());
        System.out.println("Method: " + exchange.getRequestMethod());

        switch (exchange.getRequestMethod()) {
        case "GET":
            parseGetRequest(exchange);
            break;
        case "PUT":
            parsePutRequest(exchange);
            break;
        }
    }

    private void parseGetRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().toString();

        try {
            if (path.endsWith("/users")) { // get every user
                ArrayList<String> usernames = Collections.list(users.keys());
                String response = String.join("\n", usernames);
                exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);

                OutputStream outStream = exchange.getResponseBody();
                outStream.write(response.getBytes(StandardCharsets.UTF_8));
                outStream.close();
            } else if (path.endsWith("/hash") && path.contains("/users/")) { // get the password hash
                // return user hash
                String username = exchange.getRequestURI().toString().replace("/hash", "").replace("/users/", "");
                System.out.println("Getting hash of " + username);

                if (!users.containsKey(username)) {
                    System.out.println("Unknown user: " + username);
                    exchange.sendResponseHeaders(404, 0);
                    exchange.close();
                    return;
                }

                User user = users.get(username);
                byte[] hash = user.getPasswordHash();
                exchange.sendResponseHeaders(200, hash.length);

                OutputStream outStream = exchange.getResponseBody();
                outStream.write(hash);
                outStream.close();

            } else if (path.contains("/users/") && path.endsWith("/points")) {
                String username = exchange.getRequestURI().toString().replace("/users/", "").replace("/points", "");

                if (users.containsKey(username)) {
                    User user = users.get(username);
                    int points = user.getPoints();
                    byte byteValue = (byte) points;
                    exchange.sendResponseHeaders(200, 1);

                    OutputStream outStream = exchange.getResponseBody();
                    outStream.write(byteValue);
                    outStream.close();
                } else {
                    exchange.sendResponseHeaders(404, 0);
                    exchange.close();
                }
            } else if (path.contains("/users/") && path.endsWith("/paths")) {
                String username = exchange.getRequestURI().toString().replace("/users/", "").replace("/paths", "");
                System.out.println("Username: " + username);

                if (users.containsKey(username)) {
                    User user = users.get(username);
                    List<String> bikePaths = user.getPaths();
                    OutputStream outStream = exchange.getResponseBody();

                    String everyPath = String.join("#", bikePaths);
                    exchange.sendResponseHeaders(200, everyPath.getBytes("UTF-8").length);
                    outStream.write(everyPath.getBytes("UTF-8"));

                    outStream.close();
                } else {
                    exchange.sendResponseHeaders(404, 0);
                    exchange.close();
                }
            } else if (path.contains("/users/")) { // check if a user exists
                // extract user
                String username = exchange.getRequestURI().toString().replace("/users/", "");

                if (users.containsKey(username)) {
                    exchange.sendResponseHeaders(200, 0);
                } else {
                    exchange.sendResponseHeaders(404, 0);
                    exchange.close();
                }
            } else {
                System.out.println("Unrecognized request: " + path);
                exchange.sendResponseHeaders(400, 0);
                exchange.close();
            }
        } catch (Exception e) {
            System.out.println("Unrecognized request: " + path);
            exchange.sendResponseHeaders(400, 0);
            exchange.close();
        }
    }

    private void parsePutRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().toString();

        try {

            if (path.contains("/users/") && path.contains("/path")) {
                String username = exchange.getRequestURI().toString().replace("/users/", "").replace("/path", "");

                if (users.containsKey(username)) {
                    InputStream inputStream = exchange.getRequestBody();
                    byte[] buffer = new byte[MAX_DIGEST_SIZE];
                    int offset = 0;
                    while (offset < MAX_DIGEST_SIZE) {
                        int bytesRead = inputStream.read(buffer, offset, MAX_DIGEST_SIZE - offset);
                        if (bytesRead == -1)
                            break;
                        offset += bytesRead;
                    }
                    byte[] data = new byte[offset];
                    System.arraycopy(buffer, 0, data, 0, offset);

                    User user = users.get(username);
                    user.addPath(new String(data, "UTF-8"));
                    exchange.sendResponseHeaders(200, 0);
                } else {
                    exchange.sendResponseHeaders(404, 0);
                    exchange.close();
                }
            } else if (path.contains("/users/") && path.contains("/points/")) {
                String intermediatePath = exchange.getRequestURI().toString().replace("/users/", "");
                String username = intermediatePath.substring(0, intermediatePath.indexOf("/"));
                String points = intermediatePath.substring(0, intermediatePath.lastIndexOf("/"));

                if (users.containsKey(username)) {
                    User user = users.get(username);
                    user.setPoints(Integer.parseInt(points));
                    exchange.sendResponseHeaders(200, 0);
                } else {
                    exchange.sendResponseHeaders(404, 0);
                    exchange.close();
                }
            } else if (path.contains("/users/")) {
                // extract user
                String username = exchange.getRequestURI().toString().replace("/users/", "");

                if (users.containsKey(username)) {
                    System.out.println("User '" + username + "' already exists");
                    exchange.sendResponseHeaders(400, 0);
                } else {
                    try {
                        InputStream inputStream = exchange.getRequestBody();
                        byte[] buffer = new byte[MAX_DIGEST_SIZE];
                        int offset = 0;
                        while (offset < MAX_DIGEST_SIZE) {
                            int bytesRead = inputStream.read(buffer, offset, MAX_DIGEST_SIZE - offset);
                            if (bytesRead == -1)
                                break;
                            offset += bytesRead;
                        }
                        byte[] data = new byte[offset];
                        System.arraycopy(buffer, 0, data, 0, offset);

                        User newUser = new User(data);
                        users.put(username, newUser);
                        exchange.sendResponseHeaders(200, 0);
                        System.out.println("Creating user '" + username + "'");
                    } catch (InvalidArgumentsException e) {
                        System.out.println("Can't create user: " + e.getMessage());
                        exchange.sendResponseHeaders(400, 0);
                    }
                }
            } else {
                System.out.println("Unrecognized request: " + path);
                exchange.sendResponseHeaders(400, 0);
            }
        } catch (Exception e) {
            System.out.println("Unrecognized request: " + path);
            exchange.sendResponseHeaders(400, 0);
            exchange.close();
        }
    }

}
