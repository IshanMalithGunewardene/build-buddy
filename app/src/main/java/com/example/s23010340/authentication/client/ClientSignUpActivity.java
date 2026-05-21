package com.example.s23010340.authentication.client;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.s23010340.client.ClientDashboardActivity;
import com.example.s23010340.R;
import com.example.s23010340.authentication.PasswordToggleUtils;
import com.example.s23010340.authentication.SessionManager;

public class ClientSignUpActivity extends AppCompatActivity {
    private ClientDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_sign_up);

        databaseHelper = new ClientDatabaseHelper(this);

        EditText emailInput = findViewById(R.id.client_email);
        EditText passwordInput = findViewById(R.id.client_password);
        EditText confirmInput = findViewById(R.id.client_confirm_password);
        PasswordToggleUtils.attach(passwordInput);
        PasswordToggleUtils.attach(confirmInput);

        findViewById(R.id.client_sign_up_container).setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirm = confirmInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = databaseHelper.insertUser(email, password);
            if (success) {
                Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
                new SessionManager(this).saveClientSession(email);
                Intent intent = new Intent(this, ClientDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Account already exists", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.client_go_sign_in).setOnClickListener(view -> {
            startActivity(new Intent(this, ClientSignInActivity.class));
        });
    }
}
