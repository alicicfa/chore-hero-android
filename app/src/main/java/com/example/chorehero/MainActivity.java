package com.example.chorehero;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
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
    private DrawerLayout drawerLayout;
    private ImageView btnMenu;

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
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);

        // Podešavanje menija (Sidebara)
        if (btnMenu != null && drawerLayout != null) {
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(androidx.core.view.GravityCompat.START));
        }

        // Klikovi na opcije u meniju
        setupMenuClicks();

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new TaskAdapter(taskList, this, false); // false = dječiji pogled
            recyclerView.setAdapter(adapter);
        }

        loadTasks();

        // Sakrivamo plus dugme za dijete
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

    private void setupMenuClicks() {
        View menuWeekly = findViewById(R.id.menuWeekly);
        View menuRewards = findViewById(R.id.menuRewards);
        View menuStats = findViewById(R.id.menuStats);

        if (menuWeekly != null) {
            menuWeekly.setOnClickListener(v -> {
                Toast.makeText(this, "Sedmični pregled - U izradi!", Toast.LENGTH_SHORT).show();
                if (drawerLayout != null) drawerLayout.closeDrawers();
            });
        }

        if (menuRewards != null) {
            menuRewards.setOnClickListener(v -> {
                Toast.makeText(this, "Sistem nagrada - U izradi!", Toast.LENGTH_SHORT).show();
                if (drawerLayout != null) drawerLayout.closeDrawers();
            });
        }

        if (menuStats != null) {
            menuStats.setOnClickListener(v -> {
                Toast.makeText(this, "Statistika - U izradi!", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onTaskClick(Task task) {}

    @Override
    public void onCheckClick(Task task) {
        task.isCompleted = !task.isCompleted;
        db.taskDao().updateTask(task);
        loadTasks();
    }

    @Override
    public void onEditClick(Task task) {}

    @Override
    public void onDeleteClick(Task task) {}
}