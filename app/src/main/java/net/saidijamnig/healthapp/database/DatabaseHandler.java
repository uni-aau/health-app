package net.saidijamnig.healthapp.database;

import android.content.Context;

import androidx.room.Room;

public class DatabaseHandler {
    private static AppDatabase db;

    private DatabaseHandler() {
        // Private constructor to prevent instantiation
    }

    public static synchronized AppDatabase getInitializeDatabase(Context context) {
        if (db == null) {
            db = Room.databaseBuilder(context, AppDatabase.class, "database")
                    .fallbackToDestructiveMigration() // Deletes whole database when version gets changed
                    .build();
        }
        return db;
    }

    public static synchronized void closeDatabase() {
        if (db != null) {
            db.close();
            db = null;
        }
    }


}
