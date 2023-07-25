package net.saidijamnig.healthapp.database;

import android.content.Context;

import androidx.room.Room;

/**
 * Utility class for managing the Room database.
 * This class provides methods to initialize and close the Room database instance.
 */
public class DatabaseHandler {
    private static AppDatabase db;

    /**
     * Private constructor to prevent instantiation of the DatabaseHandler class.
     * All members of this class are static, and there is no need to create instances of it.
     */
    private DatabaseHandler() {
        // Private constructor to prevent instantiation
    }

    /**
     * Initializes the Room database instance if it's not already initialized.
     * This method uses the Room.databaseBuilder() method to build the database.
     * It applies a fallback mechanism that will delete the whole database when the version gets changed.
     *
     * @param context The application context to create the database.
     * @return The initialized AppDatabase instance.
     */
    public static synchronized AppDatabase getInitializeDatabase(Context context) {
        if (db == null) {
            db = Room.databaseBuilder(context, AppDatabase.class, "database")
                    .fallbackToDestructiveMigration() // Deletes whole database when version gets changed
                    .build();
        }
        return db;
    }

    /**
     * Closes the Room database if it's open.
     * This method should be called when the database is no longer needed to free up resources.
     */
    public static synchronized void closeDatabase() {
        if (db != null) {
            db.close();
            db = null;
        }
    }
}

