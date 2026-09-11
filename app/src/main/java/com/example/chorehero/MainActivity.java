package com.example.chorehero;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private ExecutorService executorService;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        prefs = getSharedPreferences("ChoreHeroPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", 1);
        familyCode = prefs.getString("family_code", "HERO1234");

        recyclerView = findViewById(R.id.rvTasks);
        tvTotalPoints = findViewById(R.id.tvTotalPoints);
        btnLogout = findViewById(R.id.btnLogout);
        fabAddTask = findViewById(R.id.fabAddTask);
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);

        if (btnMenu != null && drawerLayout != null) {
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(androidx.core.view.GravityCompat.START));
        }

        setupMenuClicks();

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new TaskAdapter(taskList, this, false);
            recyclerView.setAdapter(adapter);
        }

        loadTasks();

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

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void setupMenuClicks() {
        View menuWeekly = findViewById(R.id.menuWeekly);
        View menuRewards = findViewById(R.id.menuRewards);
        View menuStats = findViewById(R.id.menuStats);

        if (menuWeekly != null) {
            menuWeekly.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, WeeklyReviewActivity.class);
                startActivity(intent);
                if (drawerLayout != null) drawerLayout.closeDrawers();
            });
        }

        if (menuRewards != null) {
            menuRewards.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RewardsActivity.class);
                startActivity(intent);
                if (drawerLayout != null) drawerLayout.closeDrawers();
            });
        }

        if (menuStats != null) {
            menuStats.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(intent);
                if (drawerLayout != null) drawerLayout.closeDrawers();
            });
        }
    }

    private void loadTasks() {
        if (db != null && db.taskDao() != null) {
            executorService.execute(() -> {
                List<Task> fromDb = db.taskDao().getTasksForFamily(familyCode);

                new Handler(Looper.getMainLooper()).post(() -> {
                    taskList.clear();
                    if (fromDb != null) {
                        taskList.addAll(fromDb);
                    }
                    if (adapter != null) {
                        adapter.setTasks(taskList);
                    }
                    calculatePointsFromList();
                });
            });
        }
    }

    private void calculatePointsFromList() {
        int earnedPoints = 0;
        for (Task t : taskList) {
            if (t.isCompleted) {
                earnedPoints += t.points;
            }
        }

        // Oduzmi bodove koji su potrošeni na nagrade
        int spentPoints = prefs.getInt("spent_points_" + familyCode, 0);
        int availablePoints = earnedPoints - spentPoints;
        if (availablePoints < 0) availablePoints = 0;

        if (tvTotalPoints != null) {
            tvTotalPoints.setText(availablePoints + " PTS");
        }
    }

    @Override
    public void onTaskClick(Task task) {}

    @Override
    public void onCheckClick(Task task) {
        executorService.execute(() -> {
            task.isCompleted = !task.isCompleted;
            db.taskDao().updateTask(task);

            List<Task> updatedList = db.taskDao().getTasksForFamily(familyCode);

            new Handler(Looper.getMainLooper()).post(() -> {
                if (task.isCompleted) {
                    prikaziBravoPoruku(task.points);
                }

                taskList.clear();
                if (updatedList != null) {
                    taskList.addAll(updatedList);
                }
                if (adapter != null) {
                    adapter.setTasks(taskList);
                }

                calculatePointsFromList();
            });
        });
    }

    @Override
    public void onEditClick(Task task) {}

    @Override
    public void onDeleteClick(Task task) {}

    public void prikaziBravoPoruku(int osvojeniBodovi) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_bravo);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView ivAvatar = dialog.findViewById(R.id.ivBravoAvatar);
        TextView tvPoruka = dialog.findViewById(R.id.tvBravoPoruka);
        TextView tvBodovi = dialog.findViewById(R.id.tvBravoBodovi);

        String ime = prefs.getString("user_name", "Heroj");
        String avatar = prefs.getString("user_avatar", "boy");

        Log.d("AVATAR_DEBUG", "Korisnik: " + ime + ", Avatar iz Prefs: " + avatar);

        tvPoruka.setText("Bravo " + ime + "!\nUspješno odrađen zadatak!");
        tvBodovi.setText("+ " + osvojeniBodovi + " bodova");

        boolean isFemale = false;
        if (avatar != null) {
            String lower = avatar.toLowerCase();
            if (lower.contains("girl") || lower.contains("zensko") || lower.contains("žensko") || lower.contains("female") || lower.contains("curica")) {
                isFemale = true;
            }
        }

        if (isFemale) {
            ivAvatar.setImageResource(R.drawable.hero_girl);
        } else {
            ivAvatar.setImageResource(R.drawable.hero_boy);
        }

        dialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 3500);
    }
}