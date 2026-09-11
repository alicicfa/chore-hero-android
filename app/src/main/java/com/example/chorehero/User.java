package com.example.chorehero;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String pin;
    public String role;        // "PARENT" ili "CHILD"
    public String familyCode;  // npr. "HERO1234"
    public int points;         // Ukupni bodovi (za dijete)
    public String avatar;      // "boy" ili "girl"

    // Konstruktor koji prima SVE podatke uključujući i avatar
    public User(String name, String pin, String role, String familyCode, int points, String avatar) {
        this.name = name;
        this.pin = pin;
        this.role = role;
        this.familyCode = familyCode;
        this.points = points;
        this.avatar = avatar;
    }
}