package com.example.chorehero;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    private TaskAdapter adapter;
    private List<Task> taskList = new ArrayList<>();

    private TextView tvTotalPoints;
    private RecyclerView rvTasks;
    private FloatingActionButton fabAddTask;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicijalizacija Baze
        db = AppDatabase.getInstance(this);

        // Povezivanje UI elemenata
        tvTotalPoints = findViewById(R.id.tvTotalPoints);
        rvTasks = findViewById(R.id.rvTasks);
        fabAddTask = findViewById(R.id.fabAddTask);
        btnLogout = findViewById(R.id.btnLogout);

        // Logika za odjavu
        btnLogout.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences("ChoreHeroPrefs", MODE_PRIVATE);
            preferences.edit().clear().apply();

            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        // Postavljanje RecyclerView-a
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(taskList, (task, isChecked) -> {
            // Kada korisnik štiklira/odštiklira zadatak
            db.taskDao().update(task);
            updateTotalPoints();
        });
        rvTasks.setAdapter(adapter);

        // Klik na + dugme otvara dijalog za dodavanje zadatka
        fabAddTask.setOnClickListener(v -> showAddTaskDialog());

        // Učitavanje podataka pri pokretanju
        loadTasks();
    }

    private void loadTasks() {
        taskList = db.taskDao().getAllTasks();
        adapter.setTasks(taskList);
        updateTotalPoints();
    }

    private void updateTotalPoints() {
        int totalPoints = db.taskDao().getTotalPoints();
        tvTotalPoints.setText(totalPoints + " PTS");
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Novi Zadatak 📝");

        // Layout za unos unutar dijaloga
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        EditText etTitle = view.findViewById(R.id.etTaskTitle);
        EditText etDescription = view.findViewById(R.id.etTaskDescription);
        EditText etPoints = view.findViewById(R.id.etTaskPoints);

        builder.setView(view);

        builder.setPositiveButton("Dodaj", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String pointsStr = etPoints.getText().toString().trim();

            if (!title.isEmpty() && !pointsStr.isEmpty()) {
                int points = Integer.parseInt(pointsStr);
                Task newTask = new Task(title, description, points, false);

                db.taskDao().insert(newTask);
                loadTasks();
                Toast.makeText(MainActivity.this, "Zadatak dodan!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Popunite naslov i bodove", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Odustani", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
}