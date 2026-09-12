package com.example.chorehero;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ParentDashboardActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList = new ArrayList<>();
    private AppDatabase db;
    private Button btnLogout;
    private FloatingActionButton fabAddTask, fabDeleteAllTasks;
    private DrawerLayout drawerLayout;
    private ImageView btnMenu;

    private int currentUserId = 1;
    private String familyCode = "HERO1234";

    private final String[] daysOfWeek = {"Ponedjeljak", "Utorak", "Srijeda", "Četvrtak", "Petak", "Subota", "Nedjelja"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        db = AppDatabase.getInstance(this);

        SharedPreferences prefs = getSharedPreferences("ChoreHeroPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", 1);
        familyCode = prefs.getString("family_code", "HERO1234");

        recyclerView = findViewById(R.id.rvTasks);
        btnLogout = findViewById(R.id.btnLogout);
        fabAddTask = findViewById(R.id.fabAddTask);
        fabDeleteAllTasks = findViewById(R.id.fabDeleteAllTasks);
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);

        if (btnMenu != null && drawerLayout != null) {
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(androidx.core.view.GravityCompat.START));
        }

        setupMenuClicks();

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new TaskAdapter(taskList, this, true);
            recyclerView.setAdapter(adapter);
        }

        loadTasks();

        if (fabAddTask != null) {
            fabAddTask.setOnClickListener(v -> showAddTaskDialog());
        }

        if (fabDeleteAllTasks != null) {
            fabDeleteAllTasks.setOnClickListener(v -> showDeleteAllConfirmationDialog());
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(ParentDashboardActivity.this, RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setupMenuClicks() {
        View menuWeekly = findViewById(R.id.menuWeekly);
        View menuRewards = findViewById(R.id.menuRewards);
        View menuStats = findViewById(R.id.menuStats);

        if (menuWeekly != null) {
            menuWeekly.setOnClickListener(v -> {
                Intent intent = new Intent(ParentDashboardActivity.this, WeeklyReviewActivity.class);
                startActivity(intent);
                if (drawerLayout != null) drawerLayout.closeDrawers();
            });
        }

        if (menuRewards != null) {
            menuRewards.setOnClickListener(v -> {
                Intent intent = new Intent(ParentDashboardActivity.this, RewardsActivity.class);
                startActivity(intent);
                if (drawerLayout != null) drawerLayout.closeDrawers();
            });
        }

        if (menuStats != null) {
            menuStats.setOnClickListener(v -> {
                Intent intent = new Intent(ParentDashboardActivity.this, StatisticsActivity.class);
                startActivity(intent);
                if (drawerLayout != null) drawerLayout.closeDrawers();
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
        }
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etDialogTitle);
        Spinner spinnerDays = dialogView.findViewById(R.id.spinnerDays);
        EditText etPoints = dialogView.findViewById(R.id.etDialogPoints);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, daysOfWeek);
        spinnerDays.setAdapter(spinnerAdapter);

        builder.setPositiveButton("Sačuvaj", (dialog, which) -> {
            String title = etTitle != null ? etTitle.getText().toString().trim() : "";
            String selectedDay = spinnerDays.getSelectedItem().toString();
            String pointsStr = etPoints != null ? etPoints.getText().toString().trim() : "5";

            if (title.isEmpty()) {
                Toast.makeText(this, "Unesite naziv zadatka!", Toast.LENGTH_SHORT).show();
                return;
            }

            int points = 5;
            try {
                points = Integer.parseInt(pointsStr);
            } catch (NumberFormatException ignored) {}

            Task newTask = new Task(title, selectedDay, points, false, currentUserId, familyCode);
            db.taskDao().insertTask(newTask);

            loadTasks();
            Toast.makeText(this, "Zadatak dodan za " + selectedDay + "!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Otkaži", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showDeleteAllConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Brisanje svih zadataka")
                .setMessage("Da li ste sigurni da želite obrisati sve zadatke? Ova akcija će ukloniti zadatke i sa djetetovog ekrana.")
                .setPositiveButton("Obriši sve", (dialog, which) -> {
                    db.taskDao().deleteAllTasksForFamily(familyCode);
                    loadTasks();
                    Toast.makeText(this, "Svi zadaci su obrisani.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Otkaži", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onTaskClick(Task task) {}

    @Override
    public void onCheckClick(Task task) {}

    @Override
    public void onEditClick(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etDialogTitle);
        Spinner spinnerDays = dialogView.findViewById(R.id.spinnerDays);
        EditText etPoints = dialogView.findViewById(R.id.etDialogPoints);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, daysOfWeek);
        spinnerDays.setAdapter(spinnerAdapter);

        if (etTitle != null) etTitle.setText(task.title);
        if (etPoints != null) etPoints.setText(String.valueOf(task.points));

        if (task.dayOfWeek != null) {
            for (int i = 0; i < daysOfWeek.length; i++) {
                if (daysOfWeek[i].equals(task.dayOfWeek)) {
                    spinnerDays.setSelection(i);
                    break;
                }
            }
        }

        builder.setPositiveButton("Izmjeni", (dialog, which) -> {
            String title = etTitle != null ? etTitle.getText().toString().trim() : task.title;
            String selectedDay = spinnerDays.getSelectedItem().toString();
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
            task.dayOfWeek = selectedDay;
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