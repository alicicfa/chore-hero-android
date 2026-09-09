package com.example.chorehero;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insertUser(User user);

    @Update
    void updateUser(User user);

    @Query("SELECT * FROM users WHERE name = :name AND pin = :pin LIMIT 1")
    User login(String name, String pin);

    @Query("SELECT * FROM users WHERE familyCode = :familyCode AND role = 'CHILD'")
    List<User> getChildrenByFamily(String familyCode);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(int userId);
}