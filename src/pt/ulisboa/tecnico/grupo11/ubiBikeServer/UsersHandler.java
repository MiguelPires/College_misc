package pt.ulisboa.tecnico.grupo11.ubiBikeServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class UsersHandler implements HttpHandler {
    private static final int MAX_DIGEST_SIZE = 1024;
    private Hashtable<String, User> users;

    public UsersHandler() {
        users = new Hashtable<String, User>();
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

        if (path.endsWith("/users")) {
            ArrayList<String> usernames = Collections.list(users.keys());
            String response = String.join("\n", usernames);
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);

            OutputStream outStream = exchange.getResponseBody();
            outStream.write(response.getBytes(StandardCharsets.UTF_8));
            outStream.close();
        } else if (path.endsWith("/hash") && path.contains("/users/")) {
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
            outStream.close();

        } else if (path.contains("/users/")) {
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
    }

    private void parsePutRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().toString();

        if (path.contains("/users/")) {
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

                    User newUser = new User(username, data);
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
    }

}
