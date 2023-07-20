package net.saidijamnig.healthapp.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Represents AppDatabase of the whole app
 * Currently contains History table (GPS)
 */
// Version always needs to be incremented, when schema changes & data was migrated
@Database(entities = {History.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract HistoryDao historyDao();
}
