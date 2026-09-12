package com.example.chorehero;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    void insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    // Metoda za brisanje svih zadataka određene porodice
    @Query("DELETE FROM tasks WHERE familyCode = :familyCode")
    void deleteAllTasksForFamily(String familyCode);

    @Query("SELECT * FROM tasks WHERE familyCode = :familyCode ORDER BY isCompleted ASC, id DESC")
    List<Task> getTasksForFamily(String familyCode);

    @Query("SELECT * FROM tasks WHERE childId = :childId ORDER BY isCompleted ASC, id DESC")
    List<Task> getTasksForChild(int childId);

    @Query("SELECT COALESCE(SUM(points), 0) FROM tasks WHERE childId = :childId AND isCompleted = 1")
    int getPointsForChild(int childId);
}