package com.example.s23010340.authentication.labour;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.s23010340.R;
import com.example.s23010340.authentication.PasswordToggleUtils;

public class LabourSignUpActivity extends AppCompatActivity {
    private LabourDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.labour_sign_up);

        databaseHelper = new LabourDatabaseHelper(this);

        EditText emailInput = findViewById(R.id.labour_email);
        EditText passwordInput = findViewById(R.id.labour_password);
        EditText confirmInput = findViewById(R.id.labour_confirm_password);
        PasswordToggleUtils.attach(passwordInput);
        PasswordToggleUtils.attach(confirmInput);

        findViewById(R.id.labour_sign_up_container).setOnClickListener(view -> {
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
                startActivity(new Intent(this, LabourSignInActivity.class));
            } else {
                Toast.makeText(this, "Account already exists", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.labour_go_sign_in).setOnClickListener(view -> {
            startActivity(new Intent(this, LabourSignInActivity.class));
        });
    }
}
