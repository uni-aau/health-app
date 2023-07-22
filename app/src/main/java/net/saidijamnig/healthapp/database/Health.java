package net.saidijamnig.healthapp.database;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Defines schema for the Health Data
 */
@Entity
public class Health {
    @PrimaryKey(autoGenerate = true) // automatically generate uid
    public int uid;

    @ColumnInfo(name = "health_water")
    public String waterAmount;

    @ColumnInfo(name = "health_food")
    public String foodAmount;

    @ColumnInfo(name = "health_pulse")
    public String lastMeasuredPulse;

    @ColumnInfo(name = "health_steps")
    public String lastStepsAmount;

    @ColumnInfo(name = "health_date")
    public String date;
}
