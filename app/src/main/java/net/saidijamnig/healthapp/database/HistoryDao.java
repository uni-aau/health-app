package net.saidijamnig.healthapp.database;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Provides methods that rest of the app uses to interact
 * with data in the History Table
 */
@Dao
public interface HistoryDao {
    @Query("SELECT * FROM history")
    List<History> getWholeHistoryEntries();

    @Insert
    void insertNewHistoryEntry(History historyEntry);

    @Query("DELETE FROM history WHERE uid = :uid")
    void deleteHistoryEntryById(int uid);

    @Query("DELETE FROM history")
    void deleteAll();
}
