package pt.ulisboa.tecnico.grupo11.ubibike;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Login extends AppCompatActivity {

    static final String serverUrl = "http://localhost:8000";

    Button loginBtn;

    EditText usernameLogin;
    EditText passwordLogin;

    TextView registerTxt;

    /*
        Testarmos
        Vermos se os hashs sao iguais e tirar um deles
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginBtn = (Button)findViewById(R.id.LoginBtn);

        usernameLogin = (EditText)findViewById(R.id.usernameTb);
        passwordLogin = (EditText)findViewById(R.id.passwordTb);

        registerTxt = (TextView)findViewById(R.id.registerTxt);
    }

    public void login (View view) throws IOException, NoSuchAlgorithmException {
        final Thread connectionThread  = new Thread(new Runnable() {
            public void run() {
                String username = usernameLogin.getText().toString();
                String password = passwordLogin.getText().toString();
                String url = serverUrl+"/users/"+username;
                int responseCode=0;
                HttpURLConnection readUsersConn;
                URL usersUrl = null;
                try {
                    usersUrl = new URL(url);
                    readUsersConn = (HttpURLConnection) usersUrl.openConnection();
                    readUsersConn.setRequestMethod("GET");
                    responseCode = readUsersConn.getResponseCode();
                    Log.d("TAG", "Server response code: " + responseCode);
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Login.this, "Network Error! Check your connection.", Toast.LENGTH_SHORT);
                        }
                    });
                    return;
                }
                if(responseCode==200)
                {
                    MessageDigest md = null;
                    try {
                        md = MessageDigest.getInstance("SHA-256");
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    md.update(password.getBytes());
                    byte byteData[] = md.digest();
                    Arrays.toString(byteData);
                    String hashBase64 = Base64.encodeToString(byteData, Base64.NO_WRAP | Base64.URL_SAFE);
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < byteData.length; i++)
                    {
                        sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
                    }
                    Log.d("TAG", "hashBase64 text: " + hashBase64 + " | stringbuffer text: " + sb.toString());
                    InputStream is = null;
                    byte[] buffer = null;
                    try {
                        is = readUsersConn.getInputStream();
                        //BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                        String line;
                        url+="/hash";
                        usersUrl = new URL(url);
                        readUsersConn = (HttpURLConnection) usersUrl.openConnection();
                        readUsersConn.setRequestMethod("GET");
                        responseCode = readUsersConn.getResponseCode();
                        buffer  = new byte[readUsersConn.getContentLength()];
                        is.read(buffer);
                        //line = rd.readLine();
                        //rd.close();
                    } catch (IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Login.this, "Network Error! Check your connection.", Toast.LENGTH_SHORT);
                            }
                        });
                        return;
                    }
                    if ( responseCode==200 && Arrays.equals(buffer, byteData))
                    {
                        Intent intent = new Intent(Login.this, Home.class);
                        startActivity(intent);
                    }
                    else
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                usernameLogin.setText("");
                                passwordLogin.setText("");
                                Toast.makeText(Login.this, "Internal Error!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                else
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            usernameLogin.setText("");
                            passwordLogin.setText("");
                            Toast.makeText(Login.this, "Wrong user data! Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        connectionThread.start();

    }

    public void loadRegister (View view)
    {
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
    }
}
