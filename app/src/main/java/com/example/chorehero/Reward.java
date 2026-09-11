package com.example.chorehero;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rewards")
public class Reward {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public int pointsCost;
    public String familyCode;
    public boolean isClaimed;

    public Reward(String title, int pointsCost, String familyCode) {
        this.title = title;
        this.pointsCost = pointsCost;
        this.familyCode = familyCode;
        this.isClaimed = false;
    }
}