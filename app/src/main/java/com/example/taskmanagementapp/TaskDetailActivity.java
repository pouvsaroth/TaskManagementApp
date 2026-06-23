package com.example.taskmanagementapp;


import android.graphics.drawable.GradientDrawable;
import android.widget.Toast;

import com.example.taskmanagementapp.database.CategoryDao;
import com.example.taskmanagementapp.model.Category;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.taskmanagementapp.database.AppDatabase;
import com.example.taskmanagementapp.database.TaskDao;
import com.example.taskmanagementapp.model.Task;
import com.example.taskmanagementapp.util.ToastUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {

    private Task task;
    private TaskDao taskDao;
    private CategoryDao categoryDao;
    
    private CheckBox cbTaskStatus;

    private TextView tvCategoryName;
    private View tagCategory;
    private ImageView imgCategoryIcon;

    private static final int REQUEST_CODE_EDIT_TASK = 102;

    private CheckBox cbComplete;
    private SwitchCompat switchTaskReminder;
    private TextView tvTaskTitle, tagPriority, tagCategory, tvDescription, tvDueDate, tvStatus, tvCreated, tvModified;
    private View tagCategoryContainer;
    private ImageView ivCategoryIcon;


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
        initViews();
        loadTaskData();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnMenu).setOnClickListener(v -> showPopupMenu(v));

        cbComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
            String now = sdf.format(new Date());
            task.setModifiedAt(now);
            tvModified.setText(now);
            
            updateTaskStatusVisuals(isChecked);
            taskDao.updateTask(task);
            if (isChecked) ToastUtils.showCustomToast(this, getString(R.string.toast_task_completed));
        });
    }

    private void initViews() {
        cbComplete = findViewById(R.id.cbComplete);
        switchTaskReminder = findViewById(R.id.switchTaskReminder);
        tvTaskTitle = findViewById(R.id.tvTaskTitle);
        tagPriority = findViewById(R.id.tagPriority);
        tagCategory = findViewById(R.id.tagCategory);
        tagCategoryContainer = findViewById(R.id.tagCategoryContainer);
        ivCategoryIcon = findViewById(R.id.ivCategoryIcon);
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

    private void loadTaskData() {
        if (task == null) return;

        tvTaskTitle.setText(task.getTitle());
        tvDescription.setText(task.getDescription() != null && !task.getDescription().isEmpty() 
                ? task.getDescription() : getString(R.string.no_description));
        tvDueDate.setText(task.getDueDate());
        
        // Priority Badge
        tagPriority.setText("! " + task.getPriority());
        int priorityBg;
        if ("High".equals(task.getPriority())) {
            priorityBg = ContextCompat.getColor(this, R.color.bg_icon_red_soft);
        } else if ("Medium".equals(task.getPriority())) {
            priorityBg = ContextCompat.getColor(this, R.color.bg_icon_orange_soft);
        } else {
            priorityBg = ContextCompat.getColor(this, R.color.bg_icon_blue_soft);
        }
        tagPriority.setBackgroundTintList(ColorStateList.valueOf(priorityBg));
        tagPriority.setTextColor(ContextCompat.getColor(this, R.color.text_primary));

        tagCategory.setText(task.getCategory());
        int catBg;
        if ("Work".equals(task.getCategory())) {
            catBg = ContextCompat.getColor(this, R.color.bg_icon_blue_soft);
        } else if ("Personal".equals(task.getCategory())) {
            catBg = ContextCompat.getColor(this, R.color.bg_icon_green_soft);
        } else if ("Study".equals(task.getCategory())) {
            catBg = ContextCompat.getColor(this, R.color.bg_icon_yellow_soft);
        } else {
            catBg = ContextCompat.getColor(this, R.color.filter_unselected_bg);
        }
        tagCategoryContainer.setBackgroundTintList(ColorStateList.valueOf(catBg));
        
        // Text and Icon are always primary color (black in light mode)
        int textColor = ContextCompat.getColor(this, R.color.text_primary);
        tagCategory.setTextColor(textColor);
        ivCategoryIcon.setImageTintList(ColorStateList.valueOf(textColor));
        
        cbComplete.setOnCheckedChangeListener(null);
        cbComplete.setChecked(task.isCompleted());
        updateTaskStatusVisuals(task.isCompleted());

        tvCreated.setText(task.getCreatedAt() != null ? task.getCreatedAt() : "---");
        tvModified.setText(task.getModifiedAt() != null ? task.getModifiedAt() : "---");

        switchTaskReminder.setOnCheckedChangeListener(null);
        switchTaskReminder.setChecked(task.isReminderEnabled());
        switchTaskReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setReminderEnabled(isChecked);
            taskDao.updateTask(task);
            ToastUtils.showCustomToast(this, isChecked ? getString(R.string.toast_reminder_enabled) : getString(R.string.toast_reminder_disabled));
        });
        
        cbComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
            String now = sdf.format(new Date());
            task.setModifiedAt(now);
            tvModified.setText(now);

            updateTaskStatusVisuals(isChecked);
            taskDao.updateTask(task);
            if (isChecked) ToastUtils.showCustomToast(this, getString(R.string.toast_task_completed));
        });
    }

    private void updateTaskStatusVisuals(boolean isCompleted) {
        if (isCompleted) {
            tvTaskTitle.setPaintFlags(tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvTaskTitle.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            tvStatus.setText(R.string.status_completed);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            tvStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.status_completed_bg)));
        } else {
            tvTaskTitle.setPaintFlags(tvTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            tvTaskTitle.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            tvStatus.setText(R.string.status_pending);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            tvStatus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.status_pending_bg)));
        }
    }

    private void showPopupMenu(View anchor) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.layout_task_detail_menu, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        TextView tvMenuComplete = popupView.findViewById(R.id.tvMenuComplete);
        tvMenuComplete.setText(task.isCompleted() ? getString(R.string.menu_mark_pending) : getString(R.string.menu_mark_complete));

        popupView.findViewById(R.id.menuEdit).setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateTaskActivity.class);
            intent.putExtra("task", task);
            startActivityForResult(intent, REQUEST_CODE_EDIT_TASK);
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.menuDuplicate).setOnClickListener(v -> {
            Task copy = new Task(0, task.getTitle() + " (Copy)", task.getDescription(), task.getDueDate(), task.getPriority(), task.getCategory(), false);
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
            String now = sdf.format(new Date());
            copy.setCreatedAt(now);
            copy.setModifiedAt(now);
            taskDao.addTask(copy);
            ToastUtils.showCustomToast(this, getString(R.string.toast_task_duplicated));
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.menuComplete).setOnClickListener(v -> {
            boolean newState = !task.isCompleted();
            task.setCompleted(newState);
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
            String now = sdf.format(new Date());
            task.setModifiedAt(now);
            tvModified.setText(now);

            cbComplete.setChecked(newState); // This triggers UI update through listener
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.menuFavorite).setOnClickListener(v -> {
            ToastUtils.showCustomToast(this, getString(R.string.toast_added_favorites));
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.menuDelete).setOnClickListener(v -> {
            popupWindow.dismiss();
            confirmDelete();
        });

        popupWindow.showAsDropDown(anchor, -300, 0);
    }

    private void confirmDelete() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_confirmation, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            taskDao.deleteTask(task);
            ToastUtils.showCustomToast(this, getString(R.string.toast_task_deleted));
            dialog.dismiss();
            finish();
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_TASK && resultCode == RESULT_OK) {
            // Re-fetch from DB to get updated info
            if (task != null) {
                task = taskDao.getTaskById(task.getId());
                loadTaskData();
            }
        }
    }
}
