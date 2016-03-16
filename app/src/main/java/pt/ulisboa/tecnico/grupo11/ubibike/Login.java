package pt.ulisboa.tecnico.grupo11.ubibike;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Login extends AppCompatActivity {
    static final String serverUrl = "http://52.49.141.249:8000";

    private EditText usernameLogin;
    private EditText passwordLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameLogin = (EditText) findViewById(R.id.usernameTb);
        passwordLogin = (EditText) findViewById(R.id.passwordTb);
    }

    public void login(View view) throws IOException, NoSuchAlgorithmException {
        new Thread(new Runnable() {
            public void run() {
              /*  Intent intent = new Intent(Login.this, Contacts.class);
                startActivity(intent);
                return;*/
                // verify if username exists
                String username = usernameLogin.getText().toString();

                if (!checkIfUserExists(username))
                    return;

                // validate password
                String password = passwordLogin.getText().toString();

                if (validatePassword(username, password)) {
                    Intent intent = new Intent(Login.this, Home.class);
                    startActivity(intent);
                }
            }
        }).start();
    }

    private boolean checkIfUserExists(String username) {
        final String url = serverUrl + "/users/" + username;

        if (username.isEmpty())
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Login.this, "Username can't be empty.", Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        }

        try {
            URL usersUrl = new URL(url);
            HttpURLConnection httpConnection = (HttpURLConnection) usersUrl.openConnection();
            httpConnection.setInstanceFollowRedirects(false);
            httpConnection.setRequestMethod("GET");
            int responseCode = httpConnection.getResponseCode();

            if (responseCode == 200)
                return true;
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Login.this, "Username doesn't exist.", Toast.LENGTH_SHORT).show();
                    }
                });
                return false;
            }
        } catch (final IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("CHECK_USER", Log.getStackTraceString(e));
                    Toast.makeText(Login.this, "Network Error! Check your connection.", Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        }
    }

    private boolean validatePassword(String username, String password) {
        byte[] passwordData;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes(Charset.forName("UTF-8")));
            passwordData = md.digest();
        } catch (final NoSuchAlgorithmException | NullPointerException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("DIGEST_PWD", Log.getStackTraceString(e));
                    Toast.makeText(Login.this, "Internal error.", Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        }

        try {
            URL url = new URL(serverUrl + "/users/" + username + "/hash");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept-Encoding", "");
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode != 200) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Login.this, "Server error.", Toast.LENGTH_SHORT).show();
                    }
                });
                return false;
            }

            // read password data
            InputStream inputStream = connection.getInputStream();
            byte[] buffer = new byte[connection.getContentLength()];
            inputStream.read(buffer);
            inputStream.close();

            if (Arrays.equals(buffer, passwordData)) {
                return true;
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        usernameLogin.setText("");
                        passwordLogin.setText("");
                        Toast.makeText(Login.this, "Wrong password.", Toast.LENGTH_SHORT).show();
                    }
                });
                return false;
            }
        } catch (final IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("GET_DIGEST", Log.getStackTraceString(e));
                    Toast.makeText(Login.this, "Network Error! Check your connection.", Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        }
    }

    public void loadRegister(View view) {
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
    }
}
