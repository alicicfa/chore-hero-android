package com.example.chorehero;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("ChoreHeroPrefs", MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);

            if (userId != -1) {
                // Ako je već prijavljen -> ide direktno na glavni Dashboard
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                // Ako nije -> ide na registraciju
                startActivity(new Intent(SplashActivity.this, RegisterActivity.class));
            }
            finish();
        }, 1500);
    }
}