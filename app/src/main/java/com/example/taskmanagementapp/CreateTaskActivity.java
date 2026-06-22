package com.example.taskmanagementapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.taskmanagementapp.database.AppDatabase;
import com.example.taskmanagementapp.database.TaskDao;
import com.example.taskmanagementapp.model.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateTaskActivity extends AppCompatActivity {

    private EditText etTaskTitle, etDescription;
    private TextView tvDueDate, titleText;
    private Spinner spinnerPriority, spinnerCategory;
    private SwitchCompat switchTaskReminder;
    private TaskDao taskDao;
    private Task existingTask;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        taskDao = AppDatabase.getInstance(this).taskDao();

        // Initialize views
        etTaskTitle = findViewById(R.id.etTaskTitle);
        etDescription = findViewById(R.id.etDescription);
        tvDueDate = findViewById(R.id.tvDueDate);
        titleText = findViewById(R.id.titleText);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        switchTaskReminder = findViewById(R.id.switchTaskReminder);

        // Default reminder state from global settings for new tasks
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean globalReminder = prefs.getBoolean("isReminderEnabled", true);
        switchTaskReminder.setChecked(globalReminder);

        // Set up Spinners
        String[] priorities = {"Low", "Medium", "High"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
        spinnerPriority.setSelection(1); // Default to Medium

        String[] categories = {"Work", "Personal", "Study", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Check for edit mode
        Intent intent = getIntent();
        if (intent.hasExtra("task")) {
            existingTask = (Task) intent.getSerializableExtra("task");
            isEditMode = true;
            setupEditMode();
        }

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnBackText).setOnClickListener(v -> finish());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        // Date Picker
        findViewById(R.id.btnDatePicker).setOnClickListener(v -> showDatePicker());

        // Save buttons
        View.OnClickListener saveListener = v -> saveTask();
        findViewById(R.id.btnSave).setOnClickListener(saveListener);
        findViewById(R.id.btnSaveTop).setOnClickListener(saveListener);
    }

    private void setupEditMode() {
        titleText.setText("Edit Task");
        etTaskTitle.setText(existingTask.getTitle());
        etDescription.setText(existingTask.getDescription());
        tvDueDate.setText(existingTask.getDueDate());
        switchTaskReminder.setChecked(existingTask.isReminderEnabled());
        
        // Set spinner selections
        ArrayAdapter priorityAdapter = (ArrayAdapter) spinnerPriority.getAdapter();
        spinnerPriority.setSelection(priorityAdapter.getPosition(existingTask.getPriority()));
        
        ArrayAdapter categoryAdapter = (ArrayAdapter) spinnerCategory.getAdapter();
        spinnerCategory.setSelection(categoryAdapter.getPosition(existingTask.getCategory()));
    }

    private void showDatePicker() {
        Date currentDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
            currentDate = sdf.parse(tvDueDate.getText().toString());
        } catch (Exception e) {
            currentDate = new Date();
        }

        DatePickerBottomSheet datePicker = DatePickerBottomSheet.newInstance(currentDate, date -> {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
            tvDueDate.setText(sdf.format(date));
        });
        datePicker.show(getSupportFragmentManager(), "DatePicker");
    }

    private void saveTask() {
        String title = etTaskTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String dueDate = tvDueDate.getText().toString();
        String priority = spinnerPriority.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        boolean reminderEnabled = switchTaskReminder.isChecked();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode) {
            existingTask.setTitle(title);
            existingTask.setDescription(description);
            existingTask.setDueDate(dueDate);
            existingTask.setPriority(priority);
            existingTask.setCategory(category);
            existingTask.setReminderEnabled(reminderEnabled);
            existingTask.setModifiedAt(System.currentTimeMillis());
            taskDao.updateTask(existingTask);
            showCustomToast("Task updated");
        } else {
            Task newTask = new Task(0, title, description, dueDate, priority, category, false, reminderEnabled);
            taskDao.addTask(newTask);
            showCustomToast("Task created");
        }

        setResult(RESULT_OK);
        finish();
    }

    private void showCustomToast(String message) {
        View layout = LayoutInflater.from(this).inflate(R.layout.layout_custom_toast, null);
        TextView text = layout.findViewById(R.id.toastMessage);
        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
