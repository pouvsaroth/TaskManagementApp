package com.example.taskmanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ប្រើប្រាស់ Handler ដើម្បីរង់ចាំ ៣ វិនាទី រួចប្តូរទៅកាន់ផ្ទាំង HomeActivity ស្វ័យប្រវត្តិ
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                // នៅក្នុងផ្នែក Handler run() នៃ MainActivity.java
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();


                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}