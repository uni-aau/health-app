package net.saidijamnig.healthapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class HealthFragment extends Fragment {
    private TextView stepsTextView, pulseTextView, waterTextView, foodTextView;
    private Button pulseButton, waterPlusButton, waterMinusButton, foodInputButton;

    private int stepsCount = 0;
    private int pulseRate = 0;
    private int waterCount = 0;
    private int foodCalories = 0;

    private SharedPreferences sharedPreferences;

    public HealthFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health, container, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // Verknüpfung der Views mit den XML-Elementen
        stepsTextView = view.findViewById(R.id.stepsTextView);
        pulseTextView = view.findViewById(R.id.pulseTextView);
        waterTextView = view.findViewById(R.id.waterTextView);
        foodTextView = view.findViewById(R.id.foodTextView);

        pulseButton = view.findViewById(R.id.pulseButton);
        waterPlusButton = view.findViewById(R.id.waterPlusButton);
        waterMinusButton = view.findViewById(R.id.waterMinusButton);
        foodInputButton = view.findViewById(R.id.foodInputButton);

        // Klick-Listener für die Buttons
        pulseButton.setOnClickListener(v -> measurePulse());
        waterPlusButton.setOnClickListener(v -> incrementWaterCount());
        waterMinusButton.setOnClickListener(v -> decrementWaterCount());
        foodInputButton.setOnClickListener(v -> openFoodInput());

        loadSavedData();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }

    private void loadSavedData() {
        stepsCount = sharedPreferences.getInt("stepsCount", 0);
        pulseRate = sharedPreferences.getInt("pulseRate", 0);
        waterCount = sharedPreferences.getInt("waterCount", 0);
        foodCalories = sharedPreferences.getInt("foodCalories", 0);
        updateUI();
    }

    private void saveData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("stepsCount", stepsCount);
        editor.putInt("pulseRate", pulseRate);
        editor.putInt("waterCount", waterCount);
        editor.putInt("foodCalories", foodCalories);
        editor.apply();
    }

    private void updateUI() {
        stepsTextView.setText("Steps: " + stepsCount);
        pulseTextView.setText("Pulse: " + pulseRate + " bpm");
        waterTextView.setText("Water: " + waterCount + " glasses");
        foodTextView.setText("Food: " + foodCalories + " kcal");
    }

    private void countSteps() {
        stepsCount++;
        updateUI();
    }

    private void measurePulse() {
        pulseRate = (int) (Math.random() * 100) + 50;
        updateUI();
    }

    private void incrementWaterCount() {
        waterCount++;
        updateUI();
    }

    private void decrementWaterCount() {
        if (waterCount > 0) {
            waterCount--;
            updateUI();
        }
    }

    private void openFoodInput() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Calories");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String calories = input.getText().toString();
                if (!calories.isEmpty()) {
                    foodCalories = Integer.parseInt(calories);
                    updateUI();
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
}
