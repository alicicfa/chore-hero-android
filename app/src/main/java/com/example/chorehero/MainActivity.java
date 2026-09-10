package com.example.chorehero;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList = new ArrayList<>();
    private AppDatabase db;
    private TextView tvTotalPoints;
    private Button btnLogout;
    private FloatingActionButton fabAddTask;

    private int currentUserId = 1;
    private String familyCode = "HERO1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        SharedPreferences prefs = getSharedPreferences("ChoreHeroPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", 1);
        familyCode = prefs.getString("family_code", "HERO1234");

        recyclerView = findViewById(R.id.rvTasks);
        tvTotalPoints = findViewById(R.id.tvTotalPoints);
        btnLogout = findViewById(R.id.btnLogout);
        fabAddTask = findViewById(R.id.fabAddTask);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            // PROMJENA OVDJE: Proslijeđujemo 'false' jer je ovo dječiji ekran
            adapter = new TaskAdapter(taskList, this, false);
            recyclerView.setAdapter(adapter);
        }

        loadTasks();

        // PROMJENA OVDJE: Potpuno sakrivamo plus dugme za dodavanje zadataka na dječijem ekranu
        if (fabAddTask != null) {
            fabAddTask.setVisibility(View.GONE);
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void loadTasks() {
        if (db != null && db.taskDao() != null) {
            taskList.clear();

            List<Task> fromDb = db.taskDao().getTasksForFamily(familyCode);
            if (fromDb != null) {
                taskList.addAll(fromDb);
            }

            if (adapter != null) {
                adapter.setTasks(taskList);
            }

            calculateTotalPoints();
        }
    }

    private void calculateTotalPoints() {
        if (db != null && db.taskDao() != null) {
            int total = db.taskDao().getPointsForChild(currentUserId);
            if (tvTotalPoints != null) {
                tvTotalPoints.setText(total + " PTS");
            }
        }
    }

    // --- Implementation of TaskAdapter.OnTaskClickListener ---

    @Override
    public void onTaskClick(Task task) {
        // Opcija za detalje po potrebi
    }

    @Override
    public void onCheckClick(Task task) {
        // Dijete može da mijenja status zadatka (da ga prekriži/završi)
        task.isCompleted = !task.isCompleted;
        db.taskDao().updateTask(task);
        loadTasks();
    }

    @Override
    public void onEditClick(Task task) {
        // Kod djeteta je ovo onemogućeno u adapteru, ali ostavljamo prazno zbog implementacije interfejsa
    }

    @Override
    public void onDeleteClick(Task task) {
        // Kod djeteta je ovo onemogućeno u adapteru, ali ostavljamo prazno zbog implementacije interfejsa
    }
}