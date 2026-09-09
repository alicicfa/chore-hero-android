package com.example.chorehero;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ParentDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Postavljamo layout za roditelja
        setContentView(R.layout.activity_parent_dashboard);

        // Preuzimanje podataka iz SharedPreferences
        SharedPreferences prefs = getSharedPreferences("ChoreHeroPrefs", MODE_PRIVATE);
        String parentName = prefs.getString("user_name", "Roditelj");

        btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(ParentDashboardActivity.this, RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}