package pt.ulisboa.tecnico.grupo11.ubiBikeServer;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class Server {
    static final int MAX_PUT_SIZE = 2048;

    public static void main(String[] args) throws Exception {
        
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        
        server.createContext("/users", new UsersHandler());
        server.createContext("/stations", new StationsHandler());

        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server running");
        System.out.println("Ctrl-C to terminate server");
        System.in.read();
      }
}
