package net.saidijamnig.healthapp.handler;

import android.content.Context;

import androidx.room.Room;

import net.saidijamnig.healthapp.database.AppDatabase;

public class DatabaseHandler {
    private static AppDatabase historyDb;

    private DatabaseHandler() {
        // Private constructor to prevent instantiation
    }

    public static synchronized AppDatabase getInitializedHistoryDatabase(Context context) {
        if (historyDb == null) {
            historyDb = Room.databaseBuilder(context, AppDatabase.class, "history")
                    .fallbackToDestructiveMigration() // Deletes whole database when version gets changed
                    .build();
        }
        return historyDb;
    }

    public static synchronized void closeHistoryDatabase() {
        if (historyDb != null) {
            historyDb.close();
        }
    }


}