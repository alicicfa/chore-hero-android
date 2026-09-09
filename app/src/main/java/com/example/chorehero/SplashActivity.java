package com.example.chorehero;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String PREF_NAME = "ChoreHeroPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView ivAvatar = findViewById(R.id.ivSplashAvatar);

        // Animacija: blago pomjeranje gore-dolje (-25px u odnosu na početnu poziciju)
        ObjectAnimator floatAnimation = ObjectAnimator.ofFloat(ivAvatar, "translationY", 0f, -25f);
        floatAnimation.setDuration(1000); // Trajanje jednog pokreta (1 sekunda)
        floatAnimation.setRepeatCount(ValueAnimator.INFINITE); // Ponavlja se beskonačno
        floatAnimation.setRepeatMode(ValueAnimator.REVERSE); // Ide gore pa nazad dole glatko
        floatAnimation.start();

        // Prikaz splash ekrana 2.5 sekunde pa prelazak dalje
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            String userName = preferences.getString("user_name", null);

            Intent intent;
            if (userName == null || userName.isEmpty()) {
                intent = new Intent(SplashActivity.this, RegisterActivity.class);
            } else {
                String userRole = preferences.getString("user_role", "KID");
                intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.putExtra("USER_ROLE", userRole);
            }

            startActivity(intent);
            finish();
        }, 2500);
    }
}