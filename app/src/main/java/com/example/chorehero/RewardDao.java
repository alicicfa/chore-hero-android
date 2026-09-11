package com.example.chorehero;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RewardDao {
    @Insert
    void insertReward(Reward reward);

    @Update
    void updateReward(Reward reward);

    @Delete
    void deleteReward(Reward reward);

    @Query("SELECT * FROM rewards WHERE familyCode = :familyCode")
    List<Reward> getRewardsForFamily(String familyCode);
}