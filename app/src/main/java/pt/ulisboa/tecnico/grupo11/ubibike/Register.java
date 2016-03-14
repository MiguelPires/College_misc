package pt.ulisboa.tecnico.grupo11.ubibike;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Register extends AppCompatActivity {

    /*
        Username confimar com o servidor se ja existe
        Por icon com info quando se clica nele (Tamanho da password, se é alfanumerico, etc.)
    */

    EditText usernameTb;
    EditText passwordTb;
    EditText confirmPasswordTb;

    Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerBtn = (Button)findViewById(R.id.regBtn);

        usernameTb = (EditText)findViewById(R.id.regUsernameTb);
        passwordTb = (EditText)findViewById(R.id.regPasswordTb);
        confirmPasswordTb = (EditText)findViewById(R.id.regConfirmPassTb);
    }

    public void register (View view)
    {
        final Thread connectionThread  = new Thread(new Runnable() {
            public void run() {
                if (!(confirmPasswordTb.getText().toString().equals(passwordTb.getText().toString())))
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Register.this, "Password does not match!", Toast.LENGTH_SHORT).show();
                            passwordTb.setText("");
                            confirmPasswordTb.setText("");
                        }
                    });
                    return;
                }
                URL newUserUrl = null;
                int responseCode = 0;
                try {
                    newUserUrl = new URL(Login.serverUrl+"/users/"+usernameTb.getText().toString());
                    MessageDigest md = null;
                    try {
                        md = MessageDigest.getInstance("SHA-256");
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    md.update(passwordTb.getText().toString().getBytes());
                    byte byteData[] = md.digest();
                    Arrays.toString(byteData);
                    // String hashBase64 = Base64.encodeToString(byteData, Base64.NO_WRAP | Base64.URL_SAFE);
                    // byte[] data = hashBase64.getBytes();

                    HttpURLConnection createUserConn = (HttpURLConnection) newUserUrl.openConnection();
                    createUserConn.setDoOutput(true);
                    createUserConn.setRequestMethod("PUT");

                    DataOutputStream wr = new DataOutputStream(createUserConn.getOutputStream());
                    wr.write(byteData);
                    wr.close();

                    responseCode = createUserConn.getResponseCode();
                    if ( responseCode == 400)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Register.this, "Username already in use!", Toast.LENGTH_SHORT);
                                usernameTb.setText("");
                                passwordTb.setText("");
                            }
                        });
                    }
                    else
                    {
                        Intent intent = new Intent(Register.this, Home.class);
                        startActivity(intent);
                    }

                } catch (java.io.IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Register.this, "Network Error! Check your connection.", Toast.LENGTH_SHORT);
                        }
                    });
                }
            }
        });
        connectionThread.start();
    }
}
