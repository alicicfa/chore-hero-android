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
            adapter = new TaskAdapter(taskList, this);
            recyclerView.setAdapter(adapter);
        }

        loadTasks();

        if (fabAddTask != null) {
            fabAddTask.setOnClickListener(v -> showAddTaskDialog());
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

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etDialogTitle);
        EditText etTime = dialogView.findViewById(R.id.etDialogTime);
        EditText etPoints = dialogView.findViewById(R.id.etDialogPoints);

        builder.setPositiveButton("Sačuvaj", (dialog, which) -> {
            String title = etTitle != null ? etTitle.getText().toString().trim() : "";
            String time = etTime != null ? etTime.getText().toString().trim() : "20:00";
            String pointsStr = etPoints != null ? etPoints.getText().toString().trim() : "5";

            if (title.isEmpty()) {
                Toast.makeText(this, "Unesite naziv zadatka!", Toast.LENGTH_SHORT).show();
                return;
            }

            int points = 5;
            try {
                points = Integer.parseInt(pointsStr);
            } catch (NumberFormatException ignored) {}

            Task newTask = new Task(title, time, points, false, currentUserId, familyCode);
            db.taskDao().insertTask(newTask);

            loadTasks();
            Toast.makeText(this, "Zadatak dodan!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Otkaži", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    // --- Implementation of TaskAdapter.OnTaskClickListener ---

    @Override
    public void onTaskClick(Task task) {
        // Opcija za detalje po potrebi
    }

    @Override
    public void onCheckClick(Task task) {
        task.isCompleted = !task.isCompleted;
        db.taskDao().updateTask(task);
        loadTasks();
    }

    @Override
    public void onEditClick(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etDialogTitle);
        EditText etTime = dialogView.findViewById(R.id.etDialogTime);
        EditText etPoints = dialogView.findViewById(R.id.etDialogPoints);

        // Popuni dijaloški prozor postojećim vrijednostima
        if (etTitle != null) etTitle.setText(task.title);
        if (etTime != null) etTime.setText(task.time);
        if (etPoints != null) etPoints.setText(String.valueOf(task.points));

        builder.setPositiveButton("Izmjeni", (dialog, which) -> {
            String title = etTitle != null ? etTitle.getText().toString().trim() : task.title;
            String time = etTime != null ? etTime.getText().toString().trim() : task.time;
            String pointsStr = etPoints != null ? etPoints.getText().toString().trim() : String.valueOf(task.points);

            if (title.isEmpty()) {
                Toast.makeText(this, "Naziv ne može biti prazan!", Toast.LENGTH_SHORT).show();
                return;
            }

            int points = task.points;
            try {
                points = Integer.parseInt(pointsStr);
            } catch (NumberFormatException ignored) {}

            task.title = title;
            task.time = time;
            task.points = points;

            db.taskDao().updateTask(task);
            loadTasks();
            Toast.makeText(this, "Zadatak ažuriran!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Otkaži", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    public void onDeleteClick(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Brisanje zadatka")
                .setMessage("Da li ste sigurni da želite obrisati zadatak \"" + task.title + "\"?")
                .setPositiveButton("Obriši", (dialog, which) -> {
                    db.taskDao().deleteTask(task);
                    loadTasks();
                    Toast.makeText(this, "Zadatak obrisan", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Otkaži", (dialog, which) -> dialog.dismiss())
                .show();
    }
}