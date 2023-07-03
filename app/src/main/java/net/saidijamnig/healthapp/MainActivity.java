package net.saidijamnig.healthapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchIntents();
    }

    private void switchIntents() {
        BottomNavigationView bottomView = findViewById(R.id.bottomNavigationView);
        bottomView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Intent intent = null;

                if (id == R.id.health) {
                    intent = new Intent(MainActivity.this, MainActivity.class);
                } else if (id == R.id.gps) {
                    intent = new Intent(MainActivity.this, MainActivity.class);
                } else if (id == R.id.compass) {
                    intent = new Intent(MainActivity.this, MainActivity.class);
                } else if (id == R.id.history) {
                    intent = new Intent(MainActivity.this, MainActivity.class);
                }

                if (intent != null) {
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }
        });


    }
}