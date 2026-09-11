package com.example.chorehero;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class WeeklyReviewActivity extends AppCompatActivity {

    private LinearLayout containerDays;
    private AppDatabase db;
    private String familyCode;

    private final String[] daysOfWeek = {
            "Ponedjeljak", "Utorak", "Srijeda", "Četvrtak", "Petak", "Subota", "Nedjelja"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_review);

        containerDays = findViewById(R.id.containerDays);
        db = AppDatabase.getInstance(this);

        familyCode = getSharedPreferences("ChoreHeroPrefs", MODE_PRIVATE)
                .getString("family_code", "HERO1234");

        loadWeeklyOverview();
    }

    private void loadWeeklyOverview() {
        containerDays.removeAllViews();
        List<Task> allTasks = db.taskDao().getTasksForFamily(familyCode);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (String day : daysOfWeek) {
            View dayView = inflater.inflate(R.layout.item_weekly_day, containerDays, false);

            TextView tvDayName = dayView.findViewById(R.id.tvDayName);
            TextView tvDayPoints = dayView.findViewById(R.id.tvDayPoints);
            LinearLayout containerDayTasks = dayView.findViewById(R.id.containerDayTasks);

            tvDayName.setText(day);

            int earnedPoints = 0;
            int totalPointsForDay = 0;
            int completedCount = 0;
            int totalCount = 0;

            for (Task task : allTasks) {
                if (task.dayOfWeek != null && task.dayOfWeek.equalsIgnoreCase(day)) {
                    totalCount++;
                    totalPointsForDay += task.points;

                    if (task.isCompleted) {
                        completedCount++;
                        earnedPoints += task.points;
                    }

                    TextView tvTaskItem = new TextView(this);
                    String statusIcon = task.isCompleted ? "✅ " : "⏳ ";
                    tvTaskItem.setText(statusIcon + task.title + " (+" + task.points + "b)");
                    tvTaskItem.setTextSize(14);
                    tvTaskItem.setTextColor(task.isCompleted ? Color.parseColor("#94A3B8") : Color.parseColor("#334155"));
                    containerDayTasks.addView(tvTaskItem);
                }
            }

            if (totalCount == 0) {
                TextView tvEmpty = new TextView(this);
                tvEmpty.setText("Nema zadataka za ovaj dan.");
                tvEmpty.setTextSize(14);
                tvEmpty.setTextColor(Color.parseColor("#94A3B8"));
                containerDayTasks.addView(tvEmpty);
                tvDayPoints.setText("0 bodova");
            } else {
                tvDayPoints.setText(earnedPoints + "/" + totalPointsForDay + " bodova");
            }

            containerDays.addView(dayView);
        }
    }
}