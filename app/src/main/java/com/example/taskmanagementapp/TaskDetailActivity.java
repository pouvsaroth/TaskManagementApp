package com.example.taskmanagementapp;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.taskmanagementapp.database.AppDatabase;
import com.example.taskmanagementapp.database.CategoryDao;
import com.example.taskmanagementapp.database.TaskDao;
import com.example.taskmanagementapp.model.Category;
import com.example.taskmanagementapp.model.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {

    private Task task;
    private TaskDao taskDao;
    private CategoryDao categoryDao;
    
    private CheckBox cbTaskStatus;
    private TextView tvTaskTitle, tvDescription, tvDueDate, tvStatus, tvCreated, tvModified;
    private TextView tagPriority, tvCategoryName;
    private View tagCategory;
    private ImageView imgCategoryIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        taskDao = AppDatabase.getInstance(this).taskDao();
        categoryDao = AppDatabase.getInstance(this).categoryDao();

        if (getIntent().hasExtra("task")) {
            task = (Task) getIntent().getSerializableExtra("task");
        }

        if (task == null) {
            finish();
            return;
        }

        initViews();
        populateData();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        cbTaskStatus = findViewById(R.id.cbTaskStatus);
        tvTaskTitle = findViewById(R.id.tvTaskTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvDueDate = findViewById(R.id.tvDueDate);
        tvStatus = findViewById(R.id.tvStatus);
        tvCreated = findViewById(R.id.tvCreated);
        tvModified = findViewById(R.id.tvModified);
        
        tagPriority = findViewById(R.id.tagPriority);
        tagCategory = findViewById(R.id.tagCategory);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        imgCategoryIcon = findViewById(R.id.imgCategoryIcon);

        cbTaskStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            task.setModifiedAt(System.currentTimeMillis());
            taskDao.updateTask(task);
            updateStatusText();
            tvModified.setText(formatDate(task.getModifiedAt()));
        });
    }

    private void populateData() {
        tvTaskTitle.setText(task.getTitle());
        tvDescription.setText(task.getDescription());
        tvDueDate.setText(task.getDueDate());
        cbTaskStatus.setChecked(task.isCompleted());
        
        updateStatusText();
        
        tvCreated.setText(formatDate(task.getCreatedAt()));
        tvModified.setText(formatDate(task.getModifiedAt()));

        // Priority Tag
        tagPriority.setText(task.getPriority().equalsIgnoreCase("High") ? "! High" : task.getPriority());
        if ("High".equalsIgnoreCase(task.getPriority())) {
            tagPriority.setBackgroundResource(R.drawable.bg_tag_priority_high);
        } else if ("Medium".equalsIgnoreCase(task.getPriority())) {
            tagPriority.setBackgroundResource(R.drawable.bg_filter_selected); // Use existing blue/orange
            tagPriority.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.priority_medium_bg));
        } else {
            tagPriority.setBackgroundResource(R.drawable.bg_filter_selected);
            tagPriority.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.priority_low_bg));
        }

        // Category Tag
        if (task.getCategory() != null && !task.getCategory().isEmpty()) {
            tagCategory.setVisibility(View.VISIBLE);
            tvCategoryName.setText(task.getCategory());
            
            Category category = categoryDao.getCategoryByName(task.getCategory());
            if (category != null) {
                try {
                    int color = Color.parseColor(category.getColor());
                    GradientDrawable bg = (GradientDrawable) tagCategory.getBackground();
                    bg.setColor(adjustAlpha(color, 0.1f)); // Light background
                    tvCategoryName.setTextColor(color);
                    imgCategoryIcon.setColorFilter(color);
                } catch (Exception e) {
                    // Fallback
                }
            }
        } else {
            tagCategory.setVisibility(View.GONE);
        }
    }

    private void updateStatusText() {
        if (task.isCompleted()) {
            tvStatus.setText("Completed");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_completed));
        } else {
            tvStatus.setText("Pending");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_pending));
        }
    }

    private String formatDate(long timestamp) {
        if (timestamp == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        return sdf.format(new Date(timestamp));
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
}
