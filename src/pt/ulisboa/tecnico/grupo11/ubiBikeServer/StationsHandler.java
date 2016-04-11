package pt.ulisboa.tecnico.grupo11.ubiBikeServer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StationsHandler implements HttpHandler {

    private List<String> stations;

    public StationsHandler() {
        stations = new ArrayList<String>();
        stations.add("38.75322986,-9.20676827");
        stations.add("38.75077,-9.19113");

    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        OutputStream outStream = exchange.getResponseBody();
        String stationsResponse = String.join(";", stations);
        byte[] byteValue = stationsResponse.getBytes("UTF-8");
        exchange.sendResponseHeaders(200, byteValue.length);

        outStream.write(byteValue);
        outStream.close();
    }
}
