package com.example.s23010340.authentication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.s23010340.R;
import android.content.Intent;
import com.example.s23010340.authentication.client.ClientSignInActivity;
import com.example.s23010340.authentication.labour.LabourSignInActivity;

public class RoleSelectActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_select);

        findViewById(R.id.client_button).setOnClickListener(view -> {
            startActivity(new Intent(this, ClientSignInActivity.class));
        });

        findViewById(R.id.labour_button).setOnClickListener(view -> {
            startActivity(new Intent(this, LabourSignInActivity.class));
        });
    }
}
