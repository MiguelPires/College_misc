package pt.ulisboa.tecnico.grupo11.ubibike;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Register extends AppCompatActivity {

    private EditText usernameTb;
    private EditText passwordTb;
    private EditText confirmPasswordTb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameTb = (EditText) findViewById(R.id.regUsernameTb);
        passwordTb = (EditText) findViewById(R.id.regPasswordTb);
        confirmPasswordTb = (EditText) findViewById(R.id.regConfirmPassTb);
    }

    public void register(View view) {
        final Thread connectionThread = new Thread(new Runnable() {
            public void run() {
                if (!(confirmPasswordTb.getText().toString().equals(passwordTb.getText().toString()))) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Register.this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
                            passwordTb.setText("");
                            confirmPasswordTb.setText("");
                        }
                    });
                    return;
                }

                try {
                    URL url = new URL(Login.serverUrl + "/users/" + usernameTb.getText().toString());
                    byte[] byteData;

                    try {
                        MessageDigest md = MessageDigest.getInstance("SHA-256");
                        md.update(passwordTb.getText().toString().getBytes(Charset.forName("UTF-8")));
                        byteData = md.digest();

                    } catch (NoSuchAlgorithmException e) {
                        Log.d("REGISTER_DIGEST_PWD", Log.getStackTraceString(e));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Register.this, "Internal error.", Toast.LENGTH_SHORT).show();
                                passwordTb.setText("");
                                confirmPasswordTb.setText("");
                            }
                        });
                        return;
                    }

                    HttpURLConnection createUserConn = (HttpURLConnection) url.openConnection();
                    createUserConn.setDoOutput(true);
                    createUserConn.setRequestMethod("PUT");

                    DataOutputStream wr = new DataOutputStream(createUserConn.getOutputStream());
                    wr.write(byteData);
                    wr.close();

                    int responseCode = createUserConn.getResponseCode();
                    if (responseCode == 400) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Register.this, "Username already in use!", Toast.LENGTH_SHORT).show();
                                usernameTb.setText("");
                                passwordTb.setText("");
                            }
                        });
                    } else {
                        Tab.username = usernameTb.getText().toString();
                        Intent intent = new Intent(Register.this, Tab.class);
                        startActivity(intent);
                    }

                } catch (final java.io.IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("REGISTRATION", Log.getStackTraceString(e));
                            Toast.makeText(Register.this, "Network Error! Check your connection.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        connectionThread.start();
    }
}
