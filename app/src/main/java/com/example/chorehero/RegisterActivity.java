package com.example.chorehero;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etPin;
    private RadioGroup rgRole;
    private RadioButton rbParent;
    private LinearLayout layoutAvatarSelection;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etPin = findViewById(R.id.etPin);
        rgRole = findViewById(R.id.rgRole);
        rbParent = findViewById(R.id.rbParent);
        layoutAvatarSelection = findViewById(R.id.layoutAvatarSelection);
        btnSave = findViewById(R.id.btnSaveProfile);

        if (rgRole != null) {
            rgRole.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rbParent) {
                    if (etPin != null) etPin.setVisibility(View.VISIBLE);
                    if (layoutAvatarSelection != null) layoutAvatarSelection.setVisibility(View.GONE);
                } else {
                    if (etPin != null) etPin.setVisibility(View.GONE);
                    if (layoutAvatarSelection != null) layoutAvatarSelection.setVisibility(View.VISIBLE);
                }
            });
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Unesite ime!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isParent = rbParent != null && rbParent.isChecked();
            String role = isParent ? "PARENT" : "CHILD";

            // Pamćenje sesije u SharedPreferences
            SharedPreferences prefs = getSharedPreferences("ChoreHeroPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("user_id", 1);
            editor.putString("user_name", name);
            editor.putString("user_role", role);
            editor.apply();

            // UVIJEK otvara tvoj originalni, dobri MainActivity Dashboard
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}