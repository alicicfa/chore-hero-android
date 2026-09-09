package com.example.chorehero;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private static final String PREF_NAME = "ChoreHeroPrefs";

    private EditText etName, etPin;
    private RadioGroup rgRole;
    private RadioButton rbKid, rbParent;
    private LinearLayout layoutAvatarSelection;
    private ImageView ivAvatarBoy, ivAvatarGirl;
    private Button btnSave;

    private String selectedAvatar = "BOY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        etName = findViewById(R.id.etName);
        etPin = findViewById(R.id.etPin);
        rgRole = findViewById(R.id.rgRole);
        rbKid = findViewById(R.id.rbKid);
        rbParent = findViewById(R.id.rbParent);
        layoutAvatarSelection = findViewById(R.id.layoutAvatarSelection);
        ivAvatarBoy = findViewById(R.id.ivAvatarBoy);
        ivAvatarGirl = findViewById(R.id.ivAvatarGirl);
        btnSave = findViewById(R.id.btnSaveProfile);

        // Dinamičko postavljanje tamnoljubičaste boje za RadioButton kružiće
        ColorStateList radioColorList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},  // Kada je označen
                        new int[]{-android.R.attr.state_checked} // Kada nije označen
                },
                new int[]{
                        Color.parseColor("#5E35B1"), // Tamnoljubičasta boja
                        Color.parseColor("#757575")  // Siva boja
                }
        );

        rbKid.setButtonTintList(radioColorList);
        rbParent.setButtonTintList(radioColorList);

        highlightSelectedAvatar();
        updateButtonState();

        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbParent) {
                etPin.setVisibility(View.VISIBLE);
                layoutAvatarSelection.setVisibility(View.GONE);
            } else {
                etPin.setVisibility(View.GONE);
                layoutAvatarSelection.setVisibility(View.VISIBLE);
            }
            updateButtonState();
        });

        ivAvatarBoy.setOnClickListener(v -> {
            selectedAvatar = "BOY";
            highlightSelectedAvatar();
        });

        ivAvatarGirl.setOnClickListener(v -> {
            selectedAvatar = "GIRL";
            highlightSelectedAvatar();
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etName.addTextChangedListener(textWatcher);
        etPin.addTextChangedListener(textWatcher);

        btnSave.setOnClickListener(v -> saveUserData());
    }

    private void highlightSelectedAvatar() {
        if ("BOY".equals(selectedAvatar)) {
            ivAvatarBoy.setBackgroundColor(Color.parseColor("#5E35B1"));
            ivAvatarBoy.setAlpha(1.0f);

            ivAvatarGirl.setBackgroundColor(Color.parseColor("#E0E0E0"));
            ivAvatarGirl.setAlpha(0.4f);
        } else {
            ivAvatarGirl.setBackgroundColor(Color.parseColor("#5E35B1"));
            ivAvatarGirl.setAlpha(1.0f);

            ivAvatarBoy.setBackgroundColor(Color.parseColor("#E0E0E0"));
            ivAvatarBoy.setAlpha(0.4f);
        }
    }

    private void updateButtonState() {
        String name = etName.getText().toString().trim();
        boolean isParent = rbParent.isChecked();

        boolean isValid;
        if (isParent) {
            String pin = etPin.getText().toString().trim();
            isValid = !name.isEmpty() && pin.length() >= 4;
        } else {
            isValid = !name.isEmpty();
        }

        if (isValid) {
            btnSave.setEnabled(true);
            btnSave.setAlpha(1.0f);
        } else {
            btnSave.setEnabled(false);
            btnSave.setAlpha(0.4f);
        }
    }

    private void saveUserData() {
        String name = etName.getText().toString().trim();
        boolean isParent = rbParent.isChecked();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_name", name);

        if (isParent) {
            String pin = etPin.getText().toString().trim();
            editor.putString("user_role", "PARENT");
            editor.putString("parent_pin", pin);
        } else {
            editor.putString("user_role", "KID");
            editor.putString("selected_avatar", selectedAvatar);
        }

        editor.apply();

        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.putExtra("USER_ROLE", isParent ? "PARENT" : "KID");
        startActivity(intent);
        finish();
    }
}