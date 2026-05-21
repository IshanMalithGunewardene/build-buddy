package com.example.s23010340.authentication.labour;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.s23010340.R;
import com.example.s23010340.authentication.PasswordToggleUtils;
import com.example.s23010340.authentication.SessionManager;
import java.util.concurrent.Executor;

public class LabourSignInActivity extends AppCompatActivity {
    private LabourDatabaseHelper databaseHelper;
    private BiometricPrompt biometricPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.labour_sign_in);

        databaseHelper = new LabourDatabaseHelper(this);

        EditText emailInput = findViewById(R.id.labour_email);
        EditText passwordInput = findViewById(R.id.labour_password);
        PasswordToggleUtils.attach(passwordInput);

        findViewById(R.id.labour_sign_in_container).setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean valid = databaseHelper.isValidUser(email, password);
            if (valid) {
                Toast.makeText(this, "Signed in", Toast.LENGTH_SHORT).show();
                new SessionManager(this).saveLabourSession(email);
                Intent intent = new Intent(this, LabourAvailabilityActivity.class);
                intent.putExtra(LabourAvailabilityActivity.EXTRA_LABOUR_EMAIL, email);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        setupBiometricPrompt();
        findViewById(R.id.labour_fingerprint).setOnClickListener(view -> {
            if (BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                == BiometricManager.BIOMETRIC_SUCCESS) {
                biometricPrompt.authenticate(buildPromptInfo());
            } else {
                Toast.makeText(this, "Biometric not available", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.labour_go_sign_up).setOnClickListener(view -> {
            startActivity(new Intent(this, LabourSignUpActivity.class));
        });
    }

    private void setupBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor,
            new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Toast.makeText(LabourSignInActivity.this, "Fingerprint verified", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(LabourSignInActivity.this, errString, Toast.LENGTH_SHORT).show();
                }
            });
    }

    private BiometricPrompt.PromptInfo buildPromptInfo() {
        return new BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fingerprint login")
            .setSubtitle("Verify your identity")
            .setNegativeButtonText("Cancel")
            .build();
    }
}
