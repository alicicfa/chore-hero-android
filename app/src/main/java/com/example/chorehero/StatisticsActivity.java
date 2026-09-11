package com.example.chorehero;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    private AppDatabase db;
    private String familyCode;
    private TextView tvStatTotalPoints, tvStatCompletionRate;
    private LinearLayout containerChart;

    private final String[] daysOfWeek = {
            "Ponedjeljak", "Utorak", "Srijeda", "Četvrtak", "Petak", "Subota", "Nedjelja"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        db = AppDatabase.getInstance(this);
        familyCode = getSharedPreferences("ChoreHeroPrefs", MODE_PRIVATE)
                .getString("family_code", "HERO1234");

        tvStatTotalPoints = findViewById(R.id.tvStatTotalPoints);
        tvStatCompletionRate = findViewById(R.id.tvStatCompletionRate);
        containerChart = findViewById(R.id.containerChart);

        loadStatistics();
    }

    private void loadStatistics() {
        if (db == null || db.taskDao() == null) return;

        List<Task> allTasks = db.taskDao().getTasksForFamily(familyCode);
        if (allTasks == null) {
            allTasks = new ArrayList<>();
        }

        int totalTasks = allTasks.size();
        int completedTasks = 0;
        int totalPointsEarned = 0;

        for (Task t : allTasks) {
            if (t.isCompleted) {
                completedTasks++;
                totalPointsEarned += t.points;
            }
        }

        if (tvStatTotalPoints != null) {
            tvStatTotalPoints.setText(totalPointsEarned + " PTS");
        }

        int rate = totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0;
        if (tvStatCompletionRate != null) {
            tvStatCompletionRate.setText(rate + "%");
        }

        if (containerChart == null) return;
        containerChart.removeAllViews();

        for (String day : daysOfWeek) {
            int dayTotal = 0;
            int dayCompleted = 0;

            for (Task t : allTasks) {
                if (t.dayOfWeek != null && t.dayOfWeek.equalsIgnoreCase(day)) {
                    dayTotal++;
                    if (t.isCompleted) {
                        dayCompleted++;
                    }
                }
            }

            // Red za svaki dan
            LinearLayout dayRow = new LinearLayout(this);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 8, 0, 8);
            dayRow.setLayoutParams(rowParams);
            dayRow.setOrientation(LinearLayout.HORIZONTAL);
            dayRow.setGravity(Gravity.CENTER_VERTICAL);

            // Skraćeni naziv dana (npr. Pon, Uto...)
            TextView tvDay = new TextView(this);
            LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(120, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvDay.setLayoutParams(dayParams);
            tvDay.setText(day.length() >= 3 ? day.substring(0, 3) : day);
            tvDay.setTextColor(Color.parseColor("#1E293B"));
            tvDay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvDay.setTypeface(null, android.graphics.Typeface.BOLD);
            dayRow.addView(tvDay);

            // Kontejner za pozadinu trakice grafikona
            LinearLayout barContainer = new LinearLayout(this);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(0, 28, 1f);
            barParams.setMargins(12, 0, 12, 0);
            barContainer.setLayoutParams(barParams);
            barContainer.setBackgroundColor(Color.parseColor("#F1F5F9"));

            // Efekat zaobljenih ivica za pozadinu grafikon trake
            GradientDrawable backgroundShape = new GradientDrawable();
            backgroundShape.setCornerRadius(14);
            backgroundShape.setColor(Color.parseColor("#F1F5F9"));
            barContainer.setBackground(backgroundShape);

            if (dayTotal > 0) {
                View filledBar = new View(this);
                float percentage = (float) dayCompleted / dayTotal;
                if (percentage > 1f) percentage = 1f;

                LinearLayout.LayoutParams filledParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.MATCH_PARENT, percentage);
                filledBar.setLayoutParams(filledParams);


                GradientDrawable fillShape = new GradientDrawable();
                fillShape.setCornerRadius(14);
                fillShape.setColor(Color.parseColor("#0D9488"));
                filledBar.setBackground(fillShape);

                barContainer.addView(filledBar);
            }
            dayRow.addView(barContainer);

            // Tekst sa omjerom (npr. 1/2)
            TextView tvCount = new TextView(this);
            tvCount.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            tvCount.setText(dayCompleted + "/" + dayTotal);
            tvCount.setTextColor(Color.parseColor("#64748B"));
            tvCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            tvCount.setTypeface(null, android.graphics.Typeface.BOLD);
            dayRow.addView(tvCount);

            containerChart.addView(dayRow);
        }
    }
}