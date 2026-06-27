package com.example.taskmanagementapp.activities;

import com.example.taskmanagementapp.R;
import com.example.taskmanagementapp.database.AppDatabase;
import com.example.taskmanagementapp.database.TaskDao;
import com.example.taskmanagementapp.model.Task;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.taskmanagementapp.Utilities.SidebarManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private TaskDao taskDao;
    private DrawerLayout drawerLayout;
    private SidebarManager sidebarManager;
    private TextView txtTotalCount, txtCompletedCount, txtPendingCount, txtOverdueCount;
    private TextView txtProgressPercent, txtDoneCount, txtToDoCount;
    private ProgressBar progressCircle;
    private ProgressBar progressHigh, progressMedium, progressLow;
    private TextView txtHighCount, txtMediumCount, txtLowCount;
    private LinearLayout containerTodayTasks, containerUpcomingTasks;
    private TextView txtNoTasksToday, txtNoUpcomingTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        drawerLayout = findViewById(R.id.drawerLayout);
        taskDao = AppDatabase.getInstance(this).taskDao();

        // Initialize UI components
        txtTotalCount = findViewById(R.id.txtTotalCount);
        txtCompletedCount = findViewById(R.id.txtCompletedCount);
        txtPendingCount = findViewById(R.id.txtPendingCount);
        txtOverdueCount = findViewById(R.id.txtOverdueCount);
        
        progressCircle = findViewById(R.id.progressCircle);
        txtProgressPercent = findViewById(R.id.txtProgressPercent);
        txtDoneCount = findViewById(R.id.txtDoneCount);
        txtToDoCount = findViewById(R.id.txtToDoCount);

        progressHigh = findViewById(R.id.progressHigh);
        progressMedium = findViewById(R.id.progressMedium);
        progressLow = findViewById(R.id.progressLow);
        
        txtHighCount = findViewById(R.id.txtHighCount);
        txtMediumCount = findViewById(R.id.txtMediumCount);
        txtLowCount = findViewById(R.id.txtLowCount);

        containerTodayTasks = findViewById(R.id.containerTodayTasks);
        containerUpcomingTasks = findViewById(R.id.containerUpcomingTasks);
        txtNoTasksToday = findViewById(R.id.txtNoTasksToday);
        txtNoUpcomingTasks = findViewById(R.id.txtNoUpcomingTasks);

        // 1. Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    return true;
                } else if (itemId == R.id.nav_tasks) {
                    startActivity(new Intent(HomeActivity.this, TaskActivity.class));
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                    return true;
                }
                return false;
            });
        }

        // 2. Settings button
        findViewById(R.id.btnSettingsTop).setOnClickListener(v -> 
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class)));

        // 3. Menu button
        findViewById(R.id.btnMenu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        sidebarManager = new SidebarManager(this, drawerLayout);
        sidebarManager.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sidebarManager != null) sidebarManager.refreshCategoryTaskCounts();
        updateDashboard();
    }

    private void updateDashboard() {
        List<Task> allTasks = taskDao.getAllTasks();
        int total = allTasks.size();
        int completedCount = 0;
        int pendingCount = 0;
        int overdueCount = 0;
        int highCount = 0, mediumCount = 0, lowCount = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        Calendar calToday = Calendar.getInstance();
        calToday.set(Calendar.HOUR_OF_DAY, 0);
        calToday.set(Calendar.MINUTE, 0);
        calToday.set(Calendar.SECOND, 0);
        calToday.set(Calendar.MILLISECOND, 0);
        Date today = calToday.getTime();

        containerTodayTasks.removeAllViews();
        containerUpcomingTasks.removeAllViews();

        boolean hasToday = false;
        boolean hasUpcoming = false;

        for (Task task : allTasks) {
            if (task.isCompleted()) {
                completedCount++;
            } else {
                pendingCount++;
            }

            // Priority counts
            if ("High".equalsIgnoreCase(task.getPriority())) highCount++;
            else if ("Medium".equalsIgnoreCase(task.getPriority())) mediumCount++;
            else lowCount++;

            // Date logic for Overdue, Today, Upcoming
            try {
                Date taskDate = sdf.parse(task.getDueDate());
                if (taskDate != null) {
                    if (!task.isCompleted() && taskDate.before(today)) {
                        overdueCount++;
                    }
                    
                    if (isSameDay(taskDate, today)) {
                        addTaskToContainer(containerTodayTasks, task);
                        hasToday = true;
                    } else if (taskDate.after(today)) {
                        addTaskToContainer(containerUpcomingTasks, task);
                        hasUpcoming = true;
                    }
                }
            } catch (Exception ignored) {}
        }

        // Update counts
        txtTotalCount.setText(String.valueOf(total));
        txtCompletedCount.setText(String.valueOf(completedCount));
        txtPendingCount.setText(String.valueOf(pendingCount));
        txtOverdueCount.setText(String.valueOf(overdueCount));

        // Progress Circle
        int progress = total > 0 ? (completedCount * 100 / total) : 0;
        progressCircle.setProgress(progress);
        txtProgressPercent.setText(String.format(Locale.ENGLISH, "%d%%", progress));
        txtDoneCount.setText(String.valueOf(completedCount));
        txtToDoCount.setText(String.valueOf(pendingCount));

        // Priority Bars
        progressHigh.setMax(total > 0 ? total : 1);
        progressHigh.setProgress(highCount);
        txtHighCount.setText(String.valueOf(highCount));

        progressMedium.setMax(total > 0 ? total : 1);
        progressMedium.setProgress(mediumCount);
        txtMediumCount.setText(String.valueOf(mediumCount));

        progressLow.setMax(total > 0 ? total : 1);
        progressLow.setProgress(lowCount);
        txtLowCount.setText(String.valueOf(lowCount));

        txtNoTasksToday.setVisibility(hasToday ? View.GONE : View.VISIBLE);
        txtNoUpcomingTasks.setVisibility(hasUpcoming ? View.GONE : View.VISIBLE);
    }

    private void addTaskToContainer(LinearLayout container, Task task) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_dashboard_task, container, false);
        View dot = view.findViewById(R.id.taskDot);
        TextView title = view.findViewById(R.id.taskTitle);
        TextView time = view.findViewById(R.id.taskTime);

        title.setText(task.getTitle());
        time.setText(task.getDueDate());

        int color;
        if ("High".equalsIgnoreCase(task.getPriority())) {
            color = ContextCompat.getColor(this, android.R.color.holo_red_light);
        } else if ("Medium".equalsIgnoreCase(task.getPriority())) {
            color = ContextCompat.getColor(this, android.R.color.holo_orange_light);
        } else {
            color = ContextCompat.getColor(this, android.R.color.darker_gray);
        }
        dot.setBackgroundColor(color);

        view.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, TaskDetailActivity.class);
            intent.putExtra("task", task);
            startActivity(intent);
        });

        container.addView(view);
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
