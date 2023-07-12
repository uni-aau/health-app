package net.saidijamnig.healthapp.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Defines schema for the GPS Activity History
 */
@Entity
public class History {
    @PrimaryKey(autoGenerate = true) // automatically generate uid
    public int uid;

    @ColumnInfo(name = "activity_date")
    public String activityDate;

    @ColumnInfo(name = "duration_status")
    public String durationStatus;

    @ColumnInfo(name = "activity_distance")
    public String activityDistance;

    @ColumnInfo(name = "activity_calories")
    public String activityCalories;
}