package com.example.taskmanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. រៀបចំ Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);

            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    return true;
                } else if (itemId == R.id.nav_tasks) {
                    Toast.makeText(HomeActivity.this, "Tasks screen is coming soon!", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (itemId == R.id.nav_settings) {
                    // បើកទៅកាន់អេក្រង់ SettingsActivity វិញ
                    Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            });
        }

        // 2. កំណត់សកម្មភាពលើប៊ូតុង Settings ផ្នែកខាងលើ (Top Gear Icon)
        ImageView btnSettingsTop = findViewById(R.id.btnSettingsTop);
        if (btnSettingsTop != null) {
            btnSettingsTop.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // 3. កំណត់សកម្មភាពលើប៊ូតុង Menu ផ្នែកខាងលើ
        ImageView btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v ->
                    Toast.makeText(HomeActivity.this, "Menu Clicked!", Toast.LENGTH_SHORT).show()
            );
        }
    }
}