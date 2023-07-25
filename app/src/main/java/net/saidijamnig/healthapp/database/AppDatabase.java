package net.saidijamnig.healthapp.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Represents AppDatabase of the whole app
 * Currently contains History (GPS) & Health table
 */
// Version always needs to be incremented, when schema changes & data was migrated
@Database(entities = {History.class, Health.class}, version = 7)
public abstract class AppDatabase extends RoomDatabase {
    public abstract HistoryDao historyDao();
    public abstract HealthDao healthDao();
}
