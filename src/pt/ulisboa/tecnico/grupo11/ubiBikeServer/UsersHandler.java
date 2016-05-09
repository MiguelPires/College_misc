package pt.ulisboa.tecnico.grupo11.ubiBikeServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class UsersHandler implements HttpHandler {
    public static Hashtable<String, User> users;

    public UsersHandler() throws UnsupportedEncodingException, InvalidArgumentsException, NoSuchAlgorithmException {
        users = new Hashtable<String, User>();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update("miguel".getBytes(Charset.forName("UTF-8")));
        User user = new User(md.digest());
        
        users.put("miguel", user);
        user.setPoints(51);
                // default path, for testing
        user.addPath("38.74924838,-9.20676827;38.76019789,-9.18283225");
        user.addPath("38.1232,-8.20676827;38.4568,-8.18283225");
        
        md.reset();
        md.update("maria".getBytes(Charset.forName("UTF-8")));
        user = new User(md.digest());
        
        users.put("maria", user);
        user.setPoints(20);
                // default path, for testing
        user.addPath("38.74924812,-9.20676827;38.76019732,-9.18283225");
        user.addPath("38.1232,-8.20676827;38.4568,-8.18283225");
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
            } else if (path.endsWith("/hash") && path.contains("/users/")) { // get the password hash
                // return user hash
                String username = exchange.getRequestURI().toString().replace("/hash", "").replace("/users/", "");
                System.out.println("Getting hash of " + username);

                if (!users.containsKey(username)) {
                    System.out.println("Unknown user: " + username);
                    exchange.sendResponseHeaders(404, 0);
                    return;
                }

                User user = users.get(username);
                byte[] hash = user.getPasswordHash();
                exchange.sendResponseHeaders(200, hash.length);

                OutputStream outStream = exchange.getResponseBody();
                outStream.write(hash);
            } else if (path.contains("/users/") && path.endsWith("/points")) {
                String username = exchange.getRequestURI().toString().replace("/users/", "").replace("/points", "");

                if (users.containsKey(username)) {
                    User user = users.get(username);
                    int points = user.getPoints();
                    String stringPoints = (new Integer(points)).toString();
                    byte[] byteValue = stringPoints.getBytes("UTF-8");
                    
                    exchange.sendResponseHeaders(200, byteValue.length);
                    OutputStream outStream = exchange.getResponseBody();
                    outStream.write(byteValue);
                } else {
                    exchange.sendResponseHeaders(404, 0);
                }
            } else if (path.contains("/users/") && path.endsWith("/paths")) {
                String username = exchange.getRequestURI().toString().replace("/users/", "").replace("/paths", "");
                System.out.println("Username: " + username);

                if (users.containsKey(username)) {
                    User user = users.get(username);
                    List<String> bikePaths = user.getPaths();

                    if (bikePaths != null && !bikePaths.isEmpty()) {
                    	OutputStream outStream = exchange.getResponseBody();

                    	String everyPath = String.join("#", bikePaths);
                    	exchange.sendResponseHeaders(200, everyPath.getBytes("UTF-8").length);
                    	outStream.write(everyPath.getBytes("UTF-8"));
                    } else {
                    	exchange.sendResponseHeaders(404, 0);
                    }
                } else {
                    exchange.sendResponseHeaders(404, 0);
                }
            } else if (path.contains("/users/") && path.endsWith("/key")) {
                String username = exchange.getRequestURI().toString().replace("/users/", "").replace("/key", "");

                if (users.containsKey(username)) {
                    User user = users.get(username);
                    byte[] publicKey = user.getKey();

                    OutputStream outStream = exchange.getResponseBody();
                    exchange.sendResponseHeaders(200, publicKey.length);
                    outStream.write(publicKey);
                } else {
                    exchange.sendResponseHeaders(404, 0);
                }
            } else if (path.contains("/users/")) { // check if a user exists
                // extract user
                String username = exchange.getRequestURI().toString().replace("/users/", "");

                if (users.containsKey(username)) {
                    exchange.sendResponseHeaders(200, 0);
                } else {
                    exchange.sendResponseHeaders(404, 0);
                }
            } else {
                System.out.println("Unrecognized request: " + path);
                exchange.sendResponseHeaders(400, 0);
            }
        } catch (Exception e) {
            System.out.println("Unrecognized request: " + path);
            exchange.sendResponseHeaders(400, 0);
        } finally {
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
                    byte[] buffer = new byte[Server.MAX_PUT_SIZE];
                    int offset = 0;
                    while (offset < Server.MAX_PUT_SIZE) {
                        int bytesRead = inputStream.read(buffer, offset, Server.MAX_PUT_SIZE - offset);
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
                }
            } else if (path.contains("/users/") && path.contains("/points/")) {
                String intermediatePath = exchange.getRequestURI().toString().replace("/users/", "");
                String username = intermediatePath.substring(0, intermediatePath.indexOf("/"));
                String points = intermediatePath.substring(intermediatePath.lastIndexOf("/")+1, intermediatePath.length());
                
                if (users.containsKey(username)) {
                    User user = users.get(username);
                    user.setPoints(Integer.parseInt(points));
                    exchange.sendResponseHeaders(200, 0);
                } else {
                    exchange.sendResponseHeaders(404, 0);
                }
            } else if (path.contains("/users/") && path.contains("/key")) {
                String intermediatePath = exchange.getRequestURI().toString().replace("/users/", "");
                String username = intermediatePath.substring(0, intermediatePath.indexOf("/"));

                if (users.containsKey(username)) {
                    InputStream inputStream = exchange.getRequestBody();
                    byte[] buffer = new byte[Server.MAX_PUT_SIZE];
                    int offset = 0;
                    while (offset < Server.MAX_PUT_SIZE) {
                        int bytesRead = inputStream.read(buffer, offset, Server.MAX_PUT_SIZE - offset);
                        if (bytesRead == -1)
                            break;
                        offset += bytesRead;
                    }
                    byte[] data = new byte[offset];
                    System.arraycopy(buffer, 0, data, 0, offset);

                    User user = users.get(username);
                    user.setKey(data);
                    exchange.sendResponseHeaders(200, 0);
                } else {
                    exchange.sendResponseHeaders(404, 0);
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
                        byte[] buffer = new byte[Server.MAX_PUT_SIZE];
                        int offset = 0;
                        while (offset < Server.MAX_PUT_SIZE) {
                            int bytesRead = inputStream.read(buffer, offset, Server.MAX_PUT_SIZE - offset);
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
            }else {
                System.out.println("Unrecognized request: " + path);
                exchange.sendResponseHeaders(400, 0);
            }
        } catch (Exception e) {
            System.out.println("Unrecognized request: " + path);
            exchange.sendResponseHeaders(400, 0);
        } finally {
            exchange.close();
        }
    }

}
