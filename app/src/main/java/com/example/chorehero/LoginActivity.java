package com.example.chorehero;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginName, etLoginPin;
    private RadioGroup rgLoginRole;
    private RadioButton rbLoginParent;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = AppDatabase.getInstance(this);

        etLoginName = findViewById(R.id.etLoginName);
        etLoginPin = findViewById(R.id.etLoginPin);
        rgLoginRole = findViewById(R.id.rgLoginRole);
        rbLoginParent = findViewById(R.id.rbLoginParent);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        // Prebacivanje u zavisnosti od uloge (Roditelj traži PIN, Dijete ne mora)
        rgLoginRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbLoginParent) {
                etLoginPin.setVisibility(View.VISIBLE);
            } else {
                etLoginPin.setVisibility(View.GONE);
            }
        });

        // Dugme za prijavu sa STROGOM PROVJEROM U BAZI
        btnLogin.setOnClickListener(v -> {
            String name = etLoginName.getText().toString().trim();
            String pin = etLoginPin.getText().toString().trim();
            boolean isParent = rbLoginParent.isChecked();

            if (name.isEmpty()) {
                Toast.makeText(this, "Unesite ime ili nadimak!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isParent && pin.isEmpty()) {
                Toast.makeText(this, "Unesite PIN za roditelja!", Toast.LENGTH_SHORT).show();
                return;
            }

            // PROVJERA U BAZI: Tražimo tačnog korisnika
            User user = db.userDao().login(name, isParent ? pin : "");

            if (user == null) {
                // Ako korisnik ne postoji ili je pogrešan PIN/ime, BLOKIRAMO ULAZ!
                Toast.makeText(this, "Greška! Profil ne postoji ili je pogrešan PIN/ime.", Toast.LENGTH_LONG).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("ChoreHeroPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("user_id", user.id);
            editor.putString("user_name", user.name);
            editor.putString("user_role", user.role);

            // Osiguravamo da se avatar ispravno povuče i spasi
            String avatarVal = user.avatar;
            if (avatarVal == null || avatarVal.trim().isEmpty()) {
                avatarVal = "boy";
            }
            editor.putString("user_avatar", avatarVal);
            editor.apply();

            Intent intent;
            if (user.role.equals("PARENT")) {
                intent = new Intent(LoginActivity.this, ParentDashboardActivity.class);
            } else {
                intent = new Intent(LoginActivity.this, MainActivity.class);
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Link da se vrati na registraciju ako nema profil
        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }
}