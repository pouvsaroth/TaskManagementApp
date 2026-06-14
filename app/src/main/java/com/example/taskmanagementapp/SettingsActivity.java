package com.example.taskmanagementapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;

public class SettingsActivity extends AppCompatActivity {

    private AutoCompleteTextView timeDropdown;
    private TextInputLayout timeDropdownContainer;
    private Switch switchReminder;
    private Switch switchDarkMode;

    // SharedPreferences សម្រាប់រក្សាទុកទិន្នន័យ
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "AppSettings";
    private static final String KEY_DARK_MODE = "isDarkMode";
    private static final String KEY_REMINDER = "isReminderEnabled";
    private static final String KEY_SELECTED_TIME = "selectedTime";

    private RelativeLayout btnPrivacyPolicy;
    private RelativeLayout btnTermsOfService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 1. ភ្ជាប់សមាសភាគ UI
        timeDropdown = findViewById(R.id.timeDropdown);
        timeDropdownContainer = findViewById(R.id.timeDropdownLayout);
        switchReminder = findViewById(R.id.switchReminder);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // 2. ទាញយកទិន្នន័យដែលបានរក្សាទុក
        boolean isDarkModeSaved = sharedPreferences.getBoolean(KEY_DARK_MODE, false);
        boolean isReminderSaved = sharedPreferences.getBoolean(KEY_REMINDER, true);
        String savedTime = sharedPreferences.getString(KEY_SELECTED_TIME, "9:00 AM");

        // កំណត់ស្ថានភាពទៅកាន់ Switch ផ្អែកលើទិន្នន័យចាស់
        switchDarkMode.setChecked(isDarkModeSaved);
        switchReminder.setChecked(isReminderSaved);

        // 3. កំណត់ដំណើរការ Dark Mode Switch
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // 4. កំណត់ដំណើរការសម្រាប់ Time Dropdown Menu
        String[] timeOptions = getResources().getStringArray(R.array.notification_times);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                timeOptions
        ) {
            @Override
            public android.widget.Filter getFilter() {
                return new android.widget.Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        results.values = timeOptions;
                        results.count = timeOptions.length;
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        notifyDataSetChanged();
                    }
                };
            }
        };

        timeDropdown.setAdapter(adapter);
        timeDropdown.setText(savedTime, false);

        timeDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTime = (String) parent.getItemAtPosition(position);
            sharedPreferences.edit().putString(KEY_SELECTED_TIME, selectedTime).apply();
            Toast.makeText(this, "Notification set to: " + selectedTime, Toast.LENGTH_SHORT).show();
        });

        // 5. កំណត់ការ បិទ/បើក ប្រអប់ម៉ោងផ្អែកលើ Switch
        toggleTimeSelection(isReminderSaved);

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_REMINDER, isChecked).apply();
            toggleTimeSelection(isChecked);
        });

        // 6. Connect Parent Layout Row Variables
        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy);
        btnTermsOfService = findViewById(R.id.btnTermsOfService);

        // 7. Bottom Navigation Logic
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_settings);

            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_tasks) {
                    Toast.makeText(SettingsActivity.this, "Tasks screen is coming soon!", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (itemId == R.id.nav_settings) {
                    return true;
                }
                return false;
            });
        }

        // 8. PRIVACY POLICY HOVER & TOUCH MANAGEMENT
        if (btnPrivacyPolicy != null) {
            // គ្រប់គ្រងពេលយកកណ្ដុរម៉ៅស៍ទៅដាក់ពីលើ (Mouse Hover)
            btnPrivacyPolicy.setOnHoverListener(new View.OnHoverListener() {
                @Override
                public boolean onHover(View view, MotionEvent event) {
                    View underline = view.findViewById(R.id.privacyUnderline);
                    if (underline != null) {
                        if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                            underline.setHovered(true);
                        } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                            underline.setHovered(false);
                        }
                    }
                    return false;
                }
            });

            // គ្រប់គ្រងពេលយកម្រាមដៃទៅប៉ះសង្កត់ (Touch/Press)
            btnPrivacyPolicy.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    View underline = view.findViewById(R.id.privacyUnderline);
                    if (underline != null) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            underline.setPressed(true);
                        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                            underline.setPressed(false);
                        }
                    }
                    return false; // ត្រូវតែលទ្ធផល false ដើម្បីកុំឱ្យស្ទះ OnTemplate Click
                }
            });

            btnPrivacyPolicy.setOnClickListener(v -> {
                String url = "https://7033246.figma.site/privacy";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            });
        }

        // 9. TERMS OF SERVICE HOVER & TOUCH MANAGEMENT
        if (btnTermsOfService != null) {
            // គ្រប់គ្រងពេលយកកណ្ដុរម៉ៅស៍ទៅដាក់ពីលើ (Mouse Hover)
            btnTermsOfService.setOnHoverListener(new View.OnHoverListener() {
                @Override
                public boolean onHover(View view, MotionEvent event) {
                    View underline = view.findViewById(R.id.termsUnderline);
                    if (underline != null) {
                        if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                            underline.setHovered(true);
                        } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                            underline.setHovered(false);
                        }
                    }
                    return false;
                }
            });

            // គ្រប់គ្រងពេលយកម្រាមដៃទៅប៉ះសង្កត់ (Touch/Press)
            btnTermsOfService.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    View underline = view.findViewById(R.id.termsUnderline);
                    if (underline != null) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            underline.setPressed(true);
                        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                            underline.setPressed(false);
                        }
                    }
                    return false; // ត្រូវតែលទ្ធផល false
                }
            });

            btnTermsOfService.setOnClickListener(v -> {
                String url = "https://7033246.figma.site/terms";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            });
        }
    }

    private void toggleTimeSelection(boolean isEnabled) {
        if (timeDropdownContainer != null && timeDropdown != null) {
            timeDropdownContainer.setEnabled(isEnabled);
            timeDropdown.setEnabled(isEnabled);
        }
    }
}