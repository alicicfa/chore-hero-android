package com.example.chorehero;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String dayOfWeek; // Umjesto vremena, sada čuvamo dan (npr. "Ponedjeljak")
    public int points;
    public boolean isCompleted;
    public int childId;        // ID djeteta kome pripada zadatak
    public String familyCode;  // Kod porodice

    public Task(String title, String dayOfWeek, int points, boolean isCompleted, int childId, String familyCode) {
        this.title = title;
        this.dayOfWeek = dayOfWeek;
        this.points = points;
        this.isCompleted = isCompleted;
        this.childId = childId;
        this.familyCode = familyCode;
    }
}