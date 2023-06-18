package net.saidijamnig.healthapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    private void countSteps() {
        stepsCount++;

        stepsTextView.setText("Steps: " + stepsCount);
    }

    private void measurePulse() {
        pulseRate = (int) (Math.random() * 100) + 50;

        pulseTextView.setText("Pulse: " + pulseRate + " bpm");
    }

    private void incrementWaterCount() {
        waterCount++;
        updateWaterCount();
    }

    private void decrementWaterCount() {
        if (waterCount > 0) {
            waterCount--;
            updateWaterCount();
        }
    }

    private void updateWaterCount() {
        waterTextView.setText("Water: " + waterCount + " glasses");
    }

    private void openFoodInput() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Calories");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String calories = input.getText().toString();
                if (!calories.isEmpty()) {
                    foodCalories = Integer.parseInt(calories);
                    updateFoodCalories();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void updateFoodCalories() {
        foodTextView.setText("Food: " + foodCalories + " kcal");

    }
}
