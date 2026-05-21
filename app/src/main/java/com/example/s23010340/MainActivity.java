package com.example.s23010340;

import android.os.Bundle;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.example.s23010340.authentication.RoleSelectActivity;
import com.example.s23010340.authentication.SessionManager;
import com.example.s23010340.authentication.labour.LabourAvailabilityActivity;
import com.example.s23010340.client.ClientDashboardActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            String role = sessionManager.getRole();
            if (SessionManager.ROLE_CLIENT.equals(role)) {
                startActivity(new Intent(this, ClientDashboardActivity.class));
                finish();
                return;
            }
            if (SessionManager.ROLE_LABOUR.equals(role)) {
                String email = sessionManager.getEmail();
                if (email != null && !email.trim().isEmpty()) {
                    Intent intent = new Intent(this, LabourAvailabilityActivity.class);
                    intent.putExtra(LabourAvailabilityActivity.EXTRA_LABOUR_EMAIL, email);
                    startActivity(intent);
                    finish();
                    return;
                }
                sessionManager.clearSession();
            }
        }

        setContentView(R.layout.activity_main);

        findViewById(R.id.get_started_button)
            .setOnClickListener(view -> {
                Intent intent = new Intent(MainActivity.this, RoleSelectActivity.class);
                startActivity(intent);
            });
    }
}
