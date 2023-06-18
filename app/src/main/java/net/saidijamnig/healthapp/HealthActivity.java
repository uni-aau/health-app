package net.saidijamnig.healthapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HealthActivity extends AppCompatActivity {
    private TextView stepsTextView, pulseTextView, waterTextView, foodTextView;
    private Button pulseButton, waterPlusButton, waterMinusButton, foodInputButton;

    // Variablen zur Speicherung der Werte
    private int stepsCount = 0;
    private int pulseRate = 0;
    private int waterCount = 0;
    private int foodCalories = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health);

        // Verknüpfung der Views mit den XML-Elementen
        stepsTextView = findViewById(R.id.stepsTextView);
        pulseTextView = findViewById(R.id.pulseTextView);
        waterTextView = findViewById(R.id.waterTextView);
        foodTextView = findViewById(R.id.foodTextView);

        pulseButton = findViewById(R.id.pulseButton);
        waterPlusButton = findViewById(R.id.waterPlusButton);
        waterMinusButton = findViewById(R.id.waterMinusButton);
        foodInputButton = findViewById(R.id.foodInputButton);

        // Klick-Listener für die Buttons
        pulseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                measurePulse();
            }
        });

        waterPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementWaterCount();
            }
        });

        waterMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementWaterCount();
            }
        });

        foodInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFoodInput();
            }
        });
    }
}
