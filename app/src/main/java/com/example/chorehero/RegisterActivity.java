package com.example.chorehero;

import android.app.AlertDialog;
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
    private ImageView ivAvatarBoy, ivAvatarGirl, btnAppInfo;
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
        btnAppInfo = findViewById(R.id.btnAppInfo);

        // Klik na info ikonicu otvara prozor sa uputstvom i opisom aplikacije
        if (btnAppInfo != null) {
            btnAppInfo.setOnClickListener(v -> showAppInfoDialog());
        }

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

    private void showAppInfoDialog() {
        String infoMessage =
                "Vizija i ideja:\n" +
                "Vizija aplikacije je da kroz igru motiviše djecu na izvršavanje svakodnevnih obaveza (učenje, spremanje sobe, higijena i sl.), pretvarajući rutinske zadatke u uspješno razvijanje pozitivnih navika i zdravih rutina.\n\n" +
                "Dobna granica i namjena:\n" +
                "• Aplikacija je namijenjena djeci uzrasta 5-11 godina.\n" +
                "• Prilagođena je za korištenje na jednom zajedničkom uređaju (telefon ili tablet) gdje roditelj i dijete preuzimaju uloge.\n\n" +
                "Kako funkcioniše:\n" +
                "• Roditeljski interfejs: Upravljanje zadacima, dodavanje i brisanje nagrada, te uvid u preuzeto.\n" +
                "• Dječiji profil: Sakupljanje bodova kroz zadatke, otključavanje nagrada i personalizirani avatari.\n\n" +
                "Uputstvo za upotrebu:\n" +
                "1. Prvim pokretanjem kreirajte profil (odaberite ulogu i kod).\n" +
                "2. Nakon uspješne registracije, svaki naredni put je dovoljno samo da se prijavite u aplikaciju.\n" +
                "3. Djeca na ekranu vide zadatke i klikom na kvačicu osvajaju bodove za nagrade.\n\n" +
                "Radujemo se ukoliko našu aplikaciju odaberete za izgradnju pozitivnih navika kod djece! ";

        new AlertDialog.Builder(this)
                .setTitle("O aplikaciji ChoreHero")
                .setMessage(infoMessage)
                .setPositiveButton("Zatvori", (dialog, which) -> dialog.dismiss())
                .show();
    }
}