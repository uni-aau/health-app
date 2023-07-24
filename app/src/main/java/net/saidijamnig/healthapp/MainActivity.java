package net.saidijamnig.healthapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import net.saidijamnig.healthapp.database.DatabaseHandler;
import net.saidijamnig.healthapp.databinding.ActivityMainBinding;
import net.saidijamnig.healthapp.fragments.CompassFragment;
import net.saidijamnig.healthapp.fragments.GpsFragment;
import net.saidijamnig.healthapp.fragments.HealthFragment;
import net.saidijamnig.healthapp.fragments.HistoryFragment;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final int DEFAULT_SELECTED_ITEM_ID = R.id.gps;
    ActivityMainBinding binding;
    private String[] motivationMessages;
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        binding.bottomNavigationView.setSelectedItemId(DEFAULT_SELECTED_ITEM_ID); // start position
        binding.floatingPoint.setOnClickListener(view -> handleFloatingPointClick());

        motivationMessages = getResources().getStringArray(R.array.motivation_messages);
        random = new Random();
        setContentView(binding.getRoot());

        startGeneralFragment();
        switchFragments();
    }

    private void handleFloatingPointClick() {
        int randomIndex = random.nextInt(motivationMessages.length);
        Toast.makeText(this, motivationMessages[randomIndex], Toast.LENGTH_SHORT).show();
    }

    private void switchFragments() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.health) {
                replaceFragment(new HealthFragment());
            } else if (id == R.id.gps) {
                replaceFragment(new GpsFragment());
            } else if (id == R.id.compass) {
                replaceFragment(new CompassFragment());
            } else if (id == R.id.history) {
                replaceFragment(new HistoryFragment());
            }
            return true;
        });
    }

    private void startGeneralFragment() {
        if (getIntent().getAction() != null && getIntent().getAction().equals("OPEN_FRAGMENT")) {
            String fragmentName = getIntent().getStringExtra("gpsFragmentOpen");
            if (fragmentName != null && fragmentName.equals("gpsTracking")) {
                replaceFragment(new GpsFragment());
            }
        } else {
            replaceFragment(new GpsFragment()); // Main page - Can be changed
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseHandler.closeDatabase();
    }
}