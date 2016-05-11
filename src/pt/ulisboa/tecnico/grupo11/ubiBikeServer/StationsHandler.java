package pt.ulisboa.tecnico.grupo11.ubiBikeServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StationsHandler implements HttpHandler {

    private Hashtable<String, List<String>> stations;

    public StationsHandler() {
        stations = new Hashtable<String, List<String>>();
        List<String> bikes = new ArrayList<String>() {
            {
                add("Bike1");
                add("Bike2");
                add("Bike3");
            }
        };
        stations.put("38.75322986,-9.20676827", bikes);
        bikes = new ArrayList<String>() {
            {
                add("Bike4");
            }
        };
        stations.put("38.75077,-9.19113", bikes);
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
                    List<String> bikes = stations.get(station);
                    stationsResponse += station + ":" + String.join(",", bikes) + ";";
                }

                byte[] byteValue = stationsResponse.getBytes("UTF-8");
                exchange.sendResponseHeaders(200, byteValue.length);

                OutputStream outStream = exchange.getResponseBody();
                outStream.write(byteValue);
            }

        } catch (IOException e) {
            exchange.sendResponseHeaders(400, 0);
        } finally {
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
                String operation = updateParts[1].substring(0, 1);
                String bike = updateParts[1].substring(1);

                List<String> bikes = stations.get(updateParts[0].trim());

                if (operation.equals("-")) {
                    bikes.remove(bike);
                    System.out.println("Bike " + bike + " was removed from station " + updateParts[0].trim());
                } else if (operation.equals("+")) {
                    bikes.add(bike);
                    System.out.println("Bike " + bike + " was added to station " + updateParts[0].trim());
                } else {
                    System.out.println("Unknown operation about station");
                    exchange.sendResponseHeaders(400, 0);
                    return;
                }

                exchange.sendResponseHeaders(200, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(400, 0);
        } finally {
            exchange.close();
        }
    }

}
