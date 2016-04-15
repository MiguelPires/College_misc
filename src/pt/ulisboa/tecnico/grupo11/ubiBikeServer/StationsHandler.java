package pt.ulisboa.tecnico.grupo11.ubiBikeServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StationsHandler implements HttpHandler {

    private Hashtable<String, Integer> stations;

    public StationsHandler() {
        stations = new Hashtable<String, Integer>();
        stations.put("38.75322986,-9.20676827", 3);
        stations.put("38.75077,-9.19113", 1);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().toString();
        System.out.println("URI: " + path);
        System.out.println("Method: " + exchange.getRequestMethod());

        switch (exchange.getRequestMethod()) {
        case "GET":
            parseGetRequest(exchange, path);
            break;
        case "PUT":
            parsePutRequest(exchange, path);
            break;
        }

    }

    private void parseGetRequest(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.endsWith("/stations")) {
                String stationsResponse = "";

                for (String station : stations.keySet()) {
                    stationsResponse += station + ":" + stations.get(station) + ";";
                }

                byte[] byteValue = stationsResponse.getBytes("UTF-8");
                exchange.sendResponseHeaders(200, byteValue.length);

                OutputStream outStream = exchange.getResponseBody();
                outStream.write(byteValue);
                outStream.close();
            }

        } catch (IOException e) {
            exchange.sendResponseHeaders(400, 0);
            exchange.close();
        }
    }

    private void parsePutRequest(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.endsWith("/stations")) {
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

                String stationData = new String(data, "UTF-8");
                String[] updateParts = stationData.split(":");
                stations.put(updateParts[0].trim(), Integer.parseInt(updateParts[1].trim()));

                exchange.sendResponseHeaders(200, 0);
                exchange.close();
            }

        } catch (IOException e) {
            exchange.sendResponseHeaders(400, 0);
            exchange.close();
        }
    }

}
