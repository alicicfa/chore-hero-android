package com.example.chorehero;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Task.class, User.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract TaskDao taskDao();
    public abstract UserDao userDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "chorehero_database")
                    .fallbackToDestructiveMigration() // Briše staru bazu ako se struktura promijeni (spriječava crash)
                    .allowMainThreadQueries()         // Omogućava upite na Main Threadu bez rušenja
                    .build();
        }
        return instance;
    }
}