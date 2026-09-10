package com.example.chorehero;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChildDashboardActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList = new ArrayList<>();
    private AppDatabase db;
    private Button btnLogout;
    private TextView tvPoints;
    private String familyCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_dashboard); // Kreirat ćemo i ovaj XML u sljedećem koraku

        db = AppDatabase.getInstance(this);

        SharedPreferences prefs = getSharedPreferences("ChoreHeroPrefs", MODE_PRIVATE);
        familyCode = prefs.getString("family_code", "HERO1234");

        recyclerView = findViewById(R.id.rvChildTasks);
        btnLogout = findViewById(R.id.btnLogoutChild);
        tvPoints = findViewById(R.id.tvChildPoints);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            // Proslijeđujemo 'false' za parametar isParent da dijete ne može editovati/brisati zadatke, nego samo mijenjati status (checkbox)
            adapter = new TaskAdapter(taskList, this, false);
            recyclerView.setAdapter(adapter);
        }

        loadTasks();

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(ChildDashboardActivity.this, RegisterActivity.class);
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
            int totalPoints = 0;
            if (fromDb != null) {
                for (Task t : fromDb) {
                    taskList.add(t);
                    if (t.isCompleted) {
                        totalPoints += t.points;
                    }
                }
            }
            if (adapter != null) {
                adapter.setTasks(taskList);
            }
            if (tvPoints != null) {
                tvPoints.setText(totalPoints + " PTS");
            }
        }
    }

    @Override
    public void onTaskClick(Task task) {}

    @Override
    public void onCheckClick(Task task) {
        // Kada dijete klikne na checkbox, mijenjamo status i spašavamo u bazu
        task.isCompleted = !task.isCompleted;
        db.taskDao().updateTask(task);
        loadTasks();
    }

    @Override
    public void onEditClick(Task task) {}

    @Override
    public void onDeleteClick(Task task) {}
}