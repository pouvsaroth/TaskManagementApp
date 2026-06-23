package com.example.taskmanagementapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.taskmanagementapp.adapter.TaskAdapter;
import com.example.taskmanagementapp.database.AppDatabase;
import com.example.taskmanagementapp.database.TaskDao;
import com.example.taskmanagementapp.model.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.taskmanagementapp.util.ToastUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private TaskDao taskDao;
    private static final int REQUEST_CODE_CREATE_TASK = 101;
    
    private TextView filterAll, filterToday, filterOverdue;
    private String currentFilter = "All";

    private View searchLayout;
    private EditText etSearch;
    private String searchQuery = "";
    
    private View emptyStateLayout;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_task);

        taskDao = AppDatabase.getInstance(this).taskDao();
        drawerLayout = findViewById(R.id.drawerLayout);

        // Setup RecyclerView
        recyclerView = findViewById(R.id.taskRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        findViewById(R.id.btnCreateTaskEmpty).setOnClickListener(v -> {
            Intent intent = new Intent(TaskActivity.this, CreateTaskActivity.class);
            startActivityForResult(intent, REQUEST_CODE_CREATE_TASK);
        });

        // Setup Filters
        filterAll = findViewById(R.id.filterAll);
        filterToday = findViewById(R.id.filterToday);
        filterOverdue = findViewById(R.id.filterOverdue);
        
        filterAll.setOnClickListener(v -> updateFilter("All"));
        filterToday.setOnClickListener(v -> updateFilter("Today"));
        filterOverdue.setOnClickListener(v -> updateFilter("Overdue"));

        // Setup Search
        searchLayout = findViewById(R.id.searchLayout);
        etSearch = findViewById(R.id.etSearch);
        ImageView searchIcon = findViewById(R.id.searchIcon);
        ImageView btnCloseSearch = findViewById(R.id.btnBackSearch); // This is the X button in new layout

        searchIcon.setOnClickListener(v -> {
            searchLayout.setVisibility(View.VISIBLE);
            etSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        btnCloseSearch.setOnClickListener(v -> {
            searchLayout.setVisibility(View.GONE);
            etSearch.setText("");
            searchQuery = "";
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
            loadTasks();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase().trim();
                loadTasks();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadTasks();

        // FAB to add task
        FloatingActionButton fab = findViewById(R.id.fabAddTask);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(TaskActivity.this, CreateTaskActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CREATE_TASK);
            });
        }

        // 1. Prepare Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_tasks);

            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(TaskActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_tasks) {
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    Intent intent = new Intent(TaskActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            });
        }

        // 2. top menu action
        ImageView btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v ->
                    drawerLayout.openDrawer(GravityCompat.START)
            );
        }

        // 3. top settings icon action (Top Gear Icon)
        ImageView settingsIcon = findViewById(R.id.settingsIcon);
        if (settingsIcon != null) {
            settingsIcon.setOnClickListener(v -> {
                Intent intent = new Intent(TaskActivity.this, SettingsActivity.class);
                startActivity(intent);
            });
        }

        setupSidebar();
        
        // Handle filter from Intent if any
        if (getIntent().hasExtra("filter")) {
            String filter = getIntent().getStringExtra("filter");
            if ("Completed".equals(filter)) {
                highlightSidebarItem(R.id.menuCompleted);
            } else if ("Pending".equals(filter)) {
                highlightSidebarItem(R.id.menuPending);
            }
        } else {
            highlightSidebarItem(R.id.menuAllTasks);
        }
    }

    private void setupSidebar() {
        findViewById(R.id.btnCloseSidebar).setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

        findViewById(R.id.menuHome).setOnClickListener(v -> {
            startActivity(new Intent(TaskActivity.this, HomeActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.menuAllTasks).setOnClickListener(v -> {
            updateFilter("All");
            highlightSidebarItem(R.id.menuAllTasks);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.menuCompleted).setOnClickListener(v -> {
            // Filter by completed (Custom logic could be added here)
            highlightSidebarItem(R.id.menuCompleted);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.menuPending).setOnClickListener(v -> {
            // Filter by pending
            highlightSidebarItem(R.id.menuPending);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.menuSettings).setOnClickListener(v -> {
            startActivity(new Intent(TaskActivity.this, SettingsActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        findViewById(R.id.btnAddCategory).setOnClickListener(v -> {
            startActivity(new Intent(TaskActivity.this, CategoryActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });
    }

    private void highlightSidebarItem(int menuId) {
        int selectedColor = ContextCompat.getColor(this, R.color.nav_selected);
        int unselectedColor = ContextCompat.getColor(this, R.color.text_primary);
        int unselectedIconColor = ContextCompat.getColor(this, R.color.nav_unselected);

        // Reset all
        resetSidebarItem(R.id.menuHome, R.id.imgHome, R.id.txtHome, R.id.dotHome, unselectedIconColor, unselectedColor);
        resetSidebarItem(R.id.menuAllTasks, R.id.imgAllTasks, R.id.txtAllTasks, 0, unselectedIconColor, unselectedColor);
        resetSidebarItem(R.id.menuCompleted, R.id.imgCompleted, R.id.txtCompleted, 0, unselectedIconColor, unselectedColor);
        resetSidebarItem(R.id.menuPending, R.id.imgPending, R.id.txtPending, 0, unselectedIconColor, unselectedColor);

        // Highlight selected
        if (menuId == R.id.menuHome) {
            setSidebarItemHighlighted(R.id.menuHome, R.id.imgHome, R.id.txtHome, R.id.dotHome, selectedColor);
        } else if (menuId == R.id.menuAllTasks) {
            setSidebarItemHighlighted(R.id.menuAllTasks, R.id.imgAllTasks, R.id.txtAllTasks, 0, selectedColor);
        } else if (menuId == R.id.menuCompleted) {
            setSidebarItemHighlighted(R.id.menuCompleted, R.id.imgCompleted, R.id.txtCompleted, 0, selectedColor);
        } else if (menuId == R.id.menuPending) {
            setSidebarItemHighlighted(R.id.menuPending, R.id.imgPending, R.id.txtPending, 0, selectedColor);
        }
    }

    private void resetSidebarItem(int layoutId, int imgId, int txtId, int dotId, int iconColor, int textColor) {
        View layout = findViewById(layoutId);
        ImageView img = findViewById(imgId);
        TextView txt = findViewById(txtId);
        if (layout != null) layout.setBackground(null);
        if (img != null) img.setColorFilter(iconColor);
        if (txt != null) txt.setTextColor(textColor);
        if (dotId != 0) {
            View dot = findViewById(dotId);
            if (dot != null) dot.setVisibility(View.GONE);
        }
    }

    private void setSidebarItemHighlighted(int layoutId, int imgId, int txtId, int dotId, int color) {
        View layout = findViewById(layoutId);
        ImageView img = findViewById(imgId);
        TextView txt = findViewById(txtId);
        if (layout != null) layout.setBackgroundResource(R.drawable.bg_sidebar_selected);
        if (img != null) img.setColorFilter(color);
        if (txt != null) txt.setTextColor(color);
        if (dotId != 0) {
            View dot = findViewById(dotId);
            if (dot != null) dot.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list whenever user returns to this activity
        loadTasks();
        updateSidebarCategories();
    }

    private void updateSidebarCategories() {
        LinearLayout container = findViewById(R.id.containerSidebarCategories);
        if (container == null) return;
        container.removeAllViews();

        List<com.example.taskmanagementapp.model.Category> categories = AppDatabase.getInstance(this).categoryDao().getAllCategories();
        List<Task> allTasks = taskDao.getAllTasks();

        for (com.example.taskmanagementapp.model.Category category : categories) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_sidebar_category, container, false);
            View dot = itemView.findViewById(R.id.catDot);
            TextView name = itemView.findViewById(R.id.catName);
            TextView count = itemView.findViewById(R.id.catCount);

            name.setText(category.getName());

            // Set color
            try {
                android.graphics.drawable.GradientDrawable bg = (android.graphics.drawable.GradientDrawable) dot.getBackground();
                bg.setColor(android.graphics.Color.parseColor(category.getColor()));
            } catch (Exception ignored) {}

            // Count tasks
            int taskCount = 0;
            for (Task task : allTasks) {
                if (category.getName().equalsIgnoreCase(task.getCategory())) {
                    taskCount++;
                }
            }
            count.setText(taskCount + (taskCount == 1 ? " task" : " tasks"));

            container.addView(itemView);
        }
    }


    private void updateFilter(String filter) {
        currentFilter = filter;
        
        // Update UI
        filterAll.setBackgroundResource(filter.equals("All") ? R.drawable.bg_filter_selected : R.drawable.bg_filter_unselected);
        filterAll.setTextColor(filter.equals("All") ? Color.WHITE : ContextCompat.getColor(this, R.color.nav_unselected));
        
        filterToday.setBackgroundResource(filter.equals("Today") ? R.drawable.bg_filter_selected : R.drawable.bg_filter_unselected);
        filterToday.setTextColor(filter.equals("Today") ? Color.WHITE : ContextCompat.getColor(this, R.color.nav_unselected));
        
        filterOverdue.setBackgroundResource(filter.equals("Overdue") ? R.drawable.bg_filter_selected : R.drawable.bg_filter_unselected);
        filterOverdue.setTextColor(filter.equals("Overdue") ? Color.WHITE : ContextCompat.getColor(this, R.color.nav_unselected));
        
        loadTasks();
    }

    private void loadTasks() {
        List<Task> allTasks = taskDao.getAllTasks();
        List<Task> filteredTasks = new ArrayList<>();
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        Calendar calToday = Calendar.getInstance();
        calToday.set(Calendar.HOUR_OF_DAY, 0);
        calToday.set(Calendar.MINUTE, 0);
        calToday.set(Calendar.SECOND, 0);
        calToday.set(Calendar.MILLISECOND, 0);
        Date today = calToday.getTime();

        for (Task task : allTasks) {
            // Apply Search Query first
            if (!searchQuery.isEmpty()) {
                if (!task.getTitle().toLowerCase().contains(searchQuery) && 
                    (task.getDescription() == null || !task.getDescription().toLowerCase().contains(searchQuery))) {
                    continue;
                }
            }

            // Apply Chips Filter
            if (currentFilter.equals("All")) {
                filteredTasks.add(task);
            } else {
                try {
                    Date taskDate = sdf.parse(task.getDueDate());
                    if (taskDate != null) {
                        if (currentFilter.equals("Today")) {
                            if (isSameDay(taskDate, today)) {
                                filteredTasks.add(task);
                            }
                        } else if (currentFilter.equals("Overdue")) {
                            if (taskDate.before(today) && !isSameDay(taskDate, today) && !task.isCompleted()) {
                                filteredTasks.add(task);
                            }
                        }
                    }
                } catch (Exception e) {
                    // If date can't be parsed, only show in "All"
                }
            }
        }

        if (filteredTasks.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            
            SharedPreferences prefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
            boolean globalReminderEnabled = prefs.getBoolean("isReminderEnabled", true);

            if (adapter == null) {
                adapter = new TaskAdapter(filteredTasks, this);
                adapter.setGlobalReminderEnabled(globalReminderEnabled);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.setGlobalReminderEnabled(globalReminderEnabled);
                adapter.setTasks(filteredTasks);
            }
        }
    }
    
    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CREATE_TASK && resultCode == RESULT_OK) {
            loadTasks();
        }
    }

    @Override
    public void onTaskClick(Task task) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra("task", task);
        startActivity(intent);
    }

    @Override
    public void onTaskEdit(Task task) {
        Intent intent = new Intent(this, CreateTaskActivity.class);
        intent.putExtra("task", task);
        startActivityForResult(intent, REQUEST_CODE_CREATE_TASK);
    }

    @Override
    public void onTaskDelete(Task task) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_confirmation, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // Ensure the dialog view doesn't get stretched or cut
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            taskDao.deleteTask(task);
            loadTasks();
            ToastUtils.showCustomToast(this, "Task deleted");
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onTaskDuplicate(Task task) {
        Task duplicatedTask = new Task(0, task.getTitle() + " (Copy)", 
                task.getDescription(), task.getDueDate(), 
                task.getPriority(), task.getCategory(), false, task.isReminderEnabled());
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        String now = sdf.format(new Date());
        duplicatedTask.setCreatedAt(now);
        duplicatedTask.setModifiedAt(now);

        taskDao.addTask(duplicatedTask);
        loadTasks();
        ToastUtils.showCustomToast(this, "Task duplicated");
    }

    @Override
    public void onTaskStatusChanged(Task task) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        task.setModifiedAt(sdf.format(new Date()));
        taskDao.updateTask(task);
        if (task.isCompleted()) {
            ToastUtils.showCustomToast(this, "Task completed!");
        }
    }

    @Override
    public void onTaskReminderToggled(Task task) {
        taskDao.updateTask(task);
        if (task.isReminderEnabled()) {
            ToastUtils.showCustomToast(this, "Reminder turned on");
        } else {
            ToastUtils.showCustomToast(this, "Reminder turned off");
        }
    }
}
