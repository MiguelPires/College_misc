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
    private static String serverIP = "localhost";
    
    public static void main(String[] args) throws IOException {
        
        
        URL newUserUrl = new URL("http://"+serverIP+":8000/users/anotheruser");
        String urlParameters = "123456879";
        byte[] data = urlParameters.getBytes(StandardCharsets.UTF_8);

        HttpURLConnection createUserConn = (HttpURLConnection) newUserUrl.openConnection();
        createUserConn.setDoOutput(true);
        createUserConn.setRequestMethod("PUT");
        
        DataOutputStream wr = new DataOutputStream(createUserConn.getOutputStream());
        wr.write(data);

        System.out.println("Server responded to PUT with: " + createUserConn.getResponseCode());

        URL usersUrl = new URL("http://"+serverIP+":8000/users");
        HttpURLConnection readUsersConn = (HttpURLConnection) usersUrl.openConnection();
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
}
