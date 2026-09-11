package com.example.chorehero;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Sačekaj npr. 1.5 sekundu da se prikaže splash screen, pa provjeri bazu
        new Handler().postDelayed(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            // Provjeravamo da li u bazi uopšte postoji ijedan registrovan korisnik
            // Možemo jednostavno provjeriti preko UserDao ili SharedPreferences
            SharedPreferences prefs = getSharedPreferences("ChoreHeroPrefs", MODE_PRIVATE);
            boolean isRegistered = prefs.getBoolean("is_registered", false);

            Intent intent;
            if (isRegistered) {
                // Ako je već registrovan, šaljemo ga na Login
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            } else {
                // Ako nije, šaljemo ga na Registraciju
                intent = new Intent(SplashActivity.this, RegisterActivity.class);
            }

            startActivity(intent);
            finish();
        }, 1500);
    }
}