package pt.ulisboa.tecnico.grupo11.ubibike;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends AppCompatActivity {

    Button loginBtn;

    EditText usernameLogin;
    EditText passwordLogin;

    TextView registerTxt;

    /*
        Fazer onclicklistener para butao login
        Fazer toast a dizer o que estava errado no login
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

    public void login (View view)
    {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    public void register (View view)
    {
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
    }
}
