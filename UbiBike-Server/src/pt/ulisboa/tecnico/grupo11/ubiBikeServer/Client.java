package pt.ulisboa.tecnico.grupo11.ubiBikeServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Client {
    private static String serverIP = "194.210.222.181";

    public static void main(String[] args) throws IOException {
        // PUT user
        {
            URL url = new URL("http://" + serverIP + ":8000/users/anotheruser");
            String urlParameters = "123456879";
            byte[] data = urlParameters.getBytes(StandardCharsets.UTF_8);

            HttpURLConnection createUserConn = (HttpURLConnection) url.openConnection();
            createUserConn.setDoOutput(true);
            createUserConn.setRequestMethod("PUT");

            DataOutputStream wr = new DataOutputStream(createUserConn.getOutputStream());
            wr.write(data);
            wr.close();
            
            System.out.println("Server responded to PUT with: " + createUserConn.getResponseCode());
        }
        
        // List all user
        {
            URL url = new URL("http://" + serverIP + ":8000/users");
            HttpURLConnection readUsersConn = (HttpURLConnection) url.openConnection();
            readUsersConn.setRequestMethod("GET");
            int responseCode = readUsersConn.getResponseCode();
            System.out.println("Server responded to GET with: " + responseCode);

            if (responseCode == 200) {
                InputStream is = readUsersConn.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;

                System.out.println("Listing users: ");
                while ((line = rd.readLine()) != null) {
                    System.out.println(line);
                }
                rd.close();
            } else {
                System.out.println("No users were found");
            }
        }

        // GET user hash
        {
            URL url = new URL("http://" + serverIP + ":8000/users/anotheruser/hash");
            HttpURLConnection readHashConn = (HttpURLConnection) url.openConnection();
            readHashConn.setRequestMethod("GET");
            int responseCode = readHashConn.getResponseCode();
            System.out.println("Server responded to GET hash with: " + responseCode);

            if (responseCode == 200) {
                InputStream is = readHashConn.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;

                while ((line = rd.readLine()) != null) {
                    System.out.println("Hash is " + line);
                }
                rd.close();
            } else {
                System.out.println("Hash not found");
            }

        }
        
    }
}
