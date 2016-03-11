package pt.ulisboa.tecnico.grupo11.ubiBikeServer;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class Server {
       
    public static void main(String[] args) throws Exception {
        
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        
        //server.createContext("/info", new InfoHandler());
        server.createContext("/users", new UsersHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server running");
        System.out.println("Ctrl-C to terminate server");
        System.in.read();
      }
}
