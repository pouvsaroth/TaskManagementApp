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
import androidx.core.content.ContextCompat;

import com.example.taskmanagementapp.database.AppDatabase;
import com.example.taskmanagementapp.database.TaskDao;
import com.example.taskmanagementapp.database.CategoryDao;
import com.example.taskmanagementapp.model.Category;
import com.example.taskmanagementapp.model.Task;

import com.example.taskmanagementapp.util.ToastUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateTaskActivity extends AppCompatActivity {

    private EditText etTaskTitle, etDescription;
    private TextView tvDueDate, titleText;
    private Spinner spinnerPriority, spinnerCategory;
    private SwitchCompat switchTaskReminder;
    private TaskDao taskDao;
    private CategoryDao categoryDao;
    private Task existingTask;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        taskDao = AppDatabase.getInstance(this).taskDao();
        categoryDao = AppDatabase.getInstance(this).categoryDao();

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

        setupCategorySpinner();

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

    private void setupCategorySpinner() {
        List<Category> categories = categoryDao.getAllCategories();
        if (categories.isEmpty()) {
            categoryDao.addCategory(new Category("Work", "#EF4444"));
            categoryDao.addCategory(new Category("Design", "#0EA5E9"));
            categoryDao.addCategory(new Category("Personal", "#10B981"));
            categoryDao.addCategory(new Category("Development", "#8B5CF6"));
            categories = categoryDao.getAllCategories();
        }

        String[] categoryNames = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            categoryNames[i] = categories.get(i).getName();
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void setupEditMode() {
        titleText.setText(R.string.edit_task_title);
        etTaskTitle.setText(existingTask.getTitle());
        etDescription.setText(existingTask.getDescription());
        tvDueDate.setText(existingTask.getDueDate());
        tvDueDate.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
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
            tvDueDate.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        });
        datePicker.show(getSupportFragmentManager(), "DatePicker");
    }

    private void saveTask() {
        String title = etTaskTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String dueDate = tvDueDate.getText().toString();
        
        // Don't save hint as date
        if (dueDate.equals(getString(R.string.hint_due_date))) {
            dueDate = "";
        }

        String priority = spinnerPriority.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        boolean reminderEnabled = switchTaskReminder.isChecked();

        if (title.isEmpty()) {
            ToastUtils.showCustomToast(this, getString(R.string.error_title_required));
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        String now = sdf.format(new Date());

        if (isEditMode) {
            existingTask.setTitle(title);
            existingTask.setDescription(description);
            existingTask.setDueDate(dueDate);
            existingTask.setPriority(priority);
            existingTask.setCategory(category);
            existingTask.setReminderEnabled(reminderEnabled);
            existingTask.setModifiedAt(now);
            
            taskDao.updateTask(existingTask);
            ToastUtils.showCustomToast(this, getString(R.string.toast_task_updated));
        } else {
            Task newTask = new Task(0, title, description, dueDate, priority, category, false, reminderEnabled);
            newTask.setCreatedAt(now);
            newTask.setModifiedAt(now);

            taskDao.addTask(newTask);
            ToastUtils.showCustomToast(this, getString(R.string.toast_task_created));
        }

        setResult(RESULT_OK);
        finish();
    }
}
