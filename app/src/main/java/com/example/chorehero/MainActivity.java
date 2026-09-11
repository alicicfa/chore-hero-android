package com.example.chorehero;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
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
                Toast.makeText(this, "Sistem nagrada - U izradi!", Toast.LENGTH_SHORT).show();
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

        if (task.isCompleted) {
            prikaziBravoPoruku(task.points);
        }

        loadTasks();
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

        SharedPreferences prefs = getSharedPreferences("ChoreHeroPrefs", MODE_PRIVATE);
        String ime = prefs.getString("user_name", "Heroj");
        String avatar = prefs.getString("user_avatar", "boy");

        Log.d("AVATAR_DEBUG", "Korisnik: " + ime + ", Avatar iz Prefs: " + avatar);

        tvPoruka.setText("Bravo " + ime + "!\nUspješno odrađen zadatak!");
        tvBodovi.setText("+ " + osvojeniBodovi + " bodova");

        // Provjeravamo vrijednost avatara stabilno i pouzdano
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

        new Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 3500);
    }
}