package net.saidijamnig.healthapp.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Represents AppDatabase of the whole app
 * Currently contains History table (GPS)
 */
@Database(entities = {History.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract HistoryDao historyDao();
}
