package net.saidijamnig.healthapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

/**
 * Provides methods that rest of the app uses to interact
 * with data in the Health Table
 */
@Dao
public interface HealthDao {
    @Query("SELECT * FROM health WHERE health_date = :date")
    Health selectEntryByCurrentDate(String date);

    @Insert
    void insertNewHealthEntry(Health healthEntry);

    @Query("DELETE FROM health")
    void deleteAll();
}
