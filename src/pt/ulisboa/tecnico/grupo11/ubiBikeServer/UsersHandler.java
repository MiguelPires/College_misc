package pt.ulisboa.tecnico.grupo11.ubiBikeServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javafx.scene.shape.Path;

public class UsersHandler implements HttpHandler {
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
            exchange.sendResponseHeaders(200, response.length());
            
            OutputStream outStream = exchange.getResponseBody();
            outStream.write(response.getBytes());
            outStream.close();
        } else if (path.endsWith("/hash") && path.contains("/users/")) {
            // return user hash
            String username = exchange.getRequestURI().toString().replace("/hash", "").replace("/users/", "");
            System.out.println("Getting hash of "+username);
            
            if (!users.containsKey(username)) {
                System.out.println("Unknown user: " + username);
                exchange.sendResponseHeaders(404, 0);
                return;
            } 
            
            User user = users.get(username);
            String hash = user.getPasswordHash();
            exchange.sendResponseHeaders(200, hash.length());
            
            OutputStream outStream = exchange.getResponseBody();
            outStream.write(hash.getBytes());
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
                    BufferedReader in = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                    String body = in.readLine();
                    User newUser = new User(username, body);
                    users.put(username, newUser);
                    exchange.sendResponseHeaders(200, 0);
                    System.out.println("Creating user '" + username + "' with hash '" + body + "'");
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
