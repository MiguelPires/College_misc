package pt.ulisboa.tecnico.grupo11.ubibike;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(confirmPasswordTb.getText().equals(passwordTb.getText())))
                {
                    Toast.makeText(Register.this, "Password does not match!", Toast.LENGTH_SHORT).show();
                    passwordTb.setText("");
                    confirmPasswordTb.setText("");
                }
            }
        });
    }
}
