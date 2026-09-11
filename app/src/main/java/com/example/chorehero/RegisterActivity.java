package com.example.chorehero;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etPin;
    private RadioGroup rgRole;
    private RadioButton rbParent;
    private LinearLayout layoutAvatarSelection;
    private ImageView ivAvatarBoy, ivAvatarGirl;
    private Button btnSave;
    private TextView tvGoToLogin;
    private AppDatabase db;
    private String selectedAvatar = "boy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = AppDatabase.getInstance(this);

        etName = findViewById(R.id.etName);
        etPin = findViewById(R.id.etPin);
        rgRole = findViewById(R.id.rgRole);
        rbParent = findViewById(R.id.rbParent);
        layoutAvatarSelection = findViewById(R.id.layoutAvatarSelection);
        ivAvatarBoy = findViewById(R.id.ivAvatarBoy);
        ivAvatarGirl = findViewById(R.id.ivAvatarGirl);
        btnSave = findViewById(R.id.btnSaveProfile);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbParent) {
                if (etPin != null) etPin.setVisibility(View.VISIBLE);
                if (layoutAvatarSelection != null) layoutAvatarSelection.setVisibility(View.GONE);
            } else {
                if (etPin != null) etPin.setVisibility(View.GONE);
                if (layoutAvatarSelection != null) layoutAvatarSelection.setVisibility(View.VISIBLE);
            }
        });

        ivAvatarBoy.setOnClickListener(v -> {
            selectedAvatar = "boy";
            ivAvatarBoy.setBackgroundColor(Color.parseColor("#5E35B1"));
            ivAvatarGirl.setBackgroundColor(Color.parseColor("#E0E0E0"));
        });

        ivAvatarGirl.setOnClickListener(v -> {
            selectedAvatar = "girl";
            ivAvatarGirl.setBackgroundColor(Color.parseColor("#5E35B1"));
            ivAvatarBoy.setBackgroundColor(Color.parseColor("#E0E0E0"));
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String pin = etPin.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Unesite ime ili nadimak!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isParent = rbParent.isChecked();
            String role = isParent ? "PARENT" : "CHILD";

            if (isParent && (pin.isEmpty() || pin.length() < 4)) {
                Toast.makeText(this, "Roditelj mora unijeti PIN od minimalno 4 cifre!", Toast.LENGTH_SHORT).show();
                return;
            }

            User existing = db.userDao().login(name, isParent ? pin : "");
            if (existing != null) {
                Toast.makeText(this, "Korisnik sa ovim imenom već postoji! Prijavite se.", Toast.LENGTH_LONG).show();
                return;
            }

            // Upisujemo korisnika sa izabranim avatarom ("boy" ili "girl")
            // Umjesto starog poziva, ovako treba izgledati u RegisterActivity.java:
            User newUser = new User(name, pin, role, "HERO1234", 0, selectedAvatar);
            db.userDao().insertUser(newUser);

            Toast.makeText(this, "Uspješno ste se registrovali! Prijavite se.", Toast.LENGTH_LONG).show();

            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        if (tvGoToLogin != null) {
            tvGoToLogin.setOnClickListener(v -> {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            });
        }
    }
}