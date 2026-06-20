package com.example.taskmanagementapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.taskmanagementapp.model.Task;

public class TaskDetailActivity extends AppCompatActivity {

    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        if (getIntent().hasExtra("task")) {
            task = (Task) getIntent().getSerializableExtra("task");
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
