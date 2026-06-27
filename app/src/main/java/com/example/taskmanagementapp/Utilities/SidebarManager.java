package com.example.taskmanagementapp.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.taskmanagementapp.activities.HomeActivity;
import com.example.taskmanagementapp.R;
import com.example.taskmanagementapp.activities.SettingsActivity;
import com.example.taskmanagementapp.activities.TaskActivity;
import com.example.taskmanagementapp.adapter.CategoryAdapter;
import com.example.taskmanagementapp.database.AppDatabase;
import com.example.taskmanagementapp.database.CategoryDao;
import com.example.taskmanagementapp.database.TaskDao;
import com.example.taskmanagementapp.model.Category;
import com.example.taskmanagementapp.model.Task;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SidebarManager {

    private Activity activity;
    private DrawerLayout drawerLayout;
    private CategoryDao categoryDao;
    private TaskDao taskDao;
    private CategoryAdapter categoryAdapter;
    private String selectedColor = "#EF4444"; // Default red
    private boolean isCategoriesExpanded = true;

    public SidebarManager(Activity activity, DrawerLayout drawerLayout) {
        this.activity = activity;
        this.drawerLayout = drawerLayout;
        this.categoryDao = AppDatabase.getInstance(activity).categoryDao();
        this.taskDao = AppDatabase.getInstance(activity).taskDao();
    }

    public void init() {
        setupQuickAccess();
        setupCategories();
        setupFooter();

        activity.findViewById(R.id.btnCloseSidebar).setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        activity.findViewById(R.id.btnAddCategory).setOnClickListener(v -> showCategoryDialog(null));
        
        setupExpandCollapse();
    }

    private void setupExpandCollapse() {
        View headerRow = activity.findViewById(R.id.categoriesHeaderRow);
        RecyclerView rv = activity.findViewById(R.id.categoryRecyclerView);
        ImageView chevron = activity.findViewById(R.id.btnExpandCategories);

        headerRow.setOnClickListener(v -> {
            isCategoriesExpanded = !isCategoriesExpanded;
            rv.setVisibility(isCategoriesExpanded ? View.VISIBLE : View.GONE);
            chevron.animate().rotation(isCategoriesExpanded ? 90 : 0).setDuration(200).start();
        });
    }

    private void setupQuickAccess() {
        activity.findViewById(R.id.navHome).setOnClickListener(v -> navigateTo(HomeActivity.class));
        activity.findViewById(R.id.navAllTasks).setOnClickListener(v -> navigateTo(TaskActivity.class, "All"));
        activity.findViewById(R.id.navCompleted).setOnClickListener(v -> navigateTo(TaskActivity.class, "Completed"));
        activity.findViewById(R.id.navPending).setOnClickListener(v -> navigateTo(TaskActivity.class, "Pending"));

        // Highlight active item
        updateActiveItem();
    }

    private void updateActiveItem() {
        // Reset all
        resetItem(R.id.navHome, R.drawable.ic_home);
        resetItem(R.id.navAllTasks, R.drawable.ic_tasks);
        resetItem(R.id.navCompleted, R.drawable.ic_check_circle);
        resetItem(R.id.navPending, R.drawable.ic_clock);

        if (activity instanceof HomeActivity) {
            highlightItem(R.id.navHome, R.drawable.ic_home);
        } else if (activity instanceof TaskActivity) {
            String filter = activity.getIntent().getStringExtra("filter");
            if (filter == null || filter.equals("All")) {
                highlightItem(R.id.navAllTasks, R.drawable.ic_tasks);
            } else if (filter.equals("Completed")) {
                highlightItem(R.id.navCompleted, R.drawable.ic_check_circle);
            } else if (filter.equals("Pending")) {
                highlightItem(R.id.navPending, R.drawable.ic_clock);
            }
        }
    }

    private void resetItem(int layoutId, int iconRes) {
        View layout = activity.findViewById(layoutId);
        layout.setBackgroundResource(android.R.color.transparent);
        ((ImageView) ((ViewGroup) layout).getChildAt(0)).setColorFilter(ContextCompat.getColor(activity, R.color.text_muted));
        ((TextView) ((ViewGroup) layout).getChildAt(1)).setTextColor(ContextCompat.getColor(activity, R.color.text_primary));
        ((TextView) ((ViewGroup) layout).getChildAt(1)).setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    private void highlightItem(int layoutId, int iconRes) {
        View layout = activity.findViewById(layoutId);
        layout.setBackgroundResource(R.drawable.bg_nav_item_selected);
        ((ImageView) ((ViewGroup) layout).getChildAt(0)).setColorFilter(ContextCompat.getColor(activity, R.color.nav_selected));
        ((TextView) ((ViewGroup) layout).getChildAt(1)).setTextColor(ContextCompat.getColor(activity, R.color.nav_selected));
        ((TextView) ((ViewGroup) layout).getChildAt(1)).setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void navigateTo(Class<?> targetClass) {
        navigateTo(targetClass, null);
    }

    private void navigateTo(Class<?> targetClass, String filter) {
        if (activity.getClass() == targetClass) {
            if (activity instanceof TaskActivity) {
                ((TaskActivity) activity).applyFilterFromSidebar(filter);
            }
        } else {
            Intent intent = new Intent(activity, targetClass);
            if (filter != null) intent.putExtra("filter", filter);
            activity.startActivity(intent);
            if (!(activity instanceof HomeActivity && targetClass == TaskActivity.class)) {
                // Keep history for home -> task usually, but depends on UX preference
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private void setupCategories() {
        RecyclerView rv = activity.findViewById(R.id.categoryRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(activity));
        
        SharedPreferences prefs = activity.getSharedPreferences("SidebarPrefs", Context.MODE_PRIVATE);
        boolean defaultsAdded = prefs.getBoolean("defaults_added", false);

        List<Category> categories = categoryDao.getAllCategories();
        if (categories.isEmpty() && !defaultsAdded) {
            categoryDao.addCategory(new Category("Work", "#EF4444"));
            categoryDao.addCategory(new Category("Design", "#0EA5E9"));
            categoryDao.addCategory(new Category("Personal", "#10B981"));
            categoryDao.addCategory(new Category("Development", "#8B5CF6"));
            
            prefs.edit().putBoolean("defaults_added", true).apply();
            categories = categoryDao.getAllCategories();
        }

        categoryAdapter = new CategoryAdapter(categories, new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(Category category) {
                navigateTo(TaskActivity.class, category.getName());
            }

            @Override
            public void onCategoryLongClick(Category category, View view) {
                showCategoryPopupMenu(category, view);
            }
        });
        rv.setAdapter(categoryAdapter);
        refreshCategoryTaskCounts();
    }

    public void refreshCategoryTaskCounts() {
        List<Task> allTasks = taskDao.getAllTasks();
        Map<String, Integer> counts = new HashMap<>();
        for (Task task : allTasks) {
            String cat = task.getCategory();
            if (cat != null) {
                Integer currentCount = counts.get(cat);
                counts.put(cat, currentCount == null ? 1 : currentCount + 1);
            }
        }
        categoryAdapter.setTaskCounts(counts);
    }

    private void setupFooter() {
        activity.findViewById(R.id.navSettings).setOnClickListener(v -> navigateTo(SettingsActivity.class));
        activity.findViewById(R.id.navHelp).setOnClickListener(v -> ToastUtils.showCustomToast(activity, activity.getString(R.string.toast_help_support)));
    }

    private void showCategoryDialog(Category category) {
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_category, null);
        AlertDialog dialog = new AlertDialog.Builder(activity).setView(dialogView).create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTitle = dialogView.findViewById(R.id.dialogTitle);
        EditText etName = dialogView.findViewById(R.id.etCategoryName);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        LinearLayout colorRow = dialogView.findViewById(R.id.colorSelectionRow);

        if (category != null) {
            tvTitle.setText("Edit Category");
            etName.setText(category.getName());
            btnSave.setText("Save");
            selectedColor = category.getColor();
        } else {
            selectedColor = "#EF4444";
        }

        // Setup color selection
        for (int i = 0; i < colorRow.getChildCount(); i++) {
            FrameLayout colorContainer = (FrameLayout) colorRow.getChildAt(i);
            View colorView = colorContainer.getChildAt(0);
            
            colorContainer.setOnClickListener(v -> {
                // Reset others
                for (int j = 0; j < colorRow.getChildCount(); j++) {
                    FrameLayout otherContainer = (FrameLayout) colorRow.getChildAt(j);
                    otherContainer.setBackground(null);
                    otherContainer.getChildAt(0).setAlpha(0.3f);
                }
                // Select current
                colorContainer.setBackgroundResource(R.drawable.bg_color_selection_border);
                colorView.setAlpha(1.0f);
                
                // Get hex
                int viewId = colorView.getId();
                if (viewId == R.id.colorRed) selectedColor = "#EF4444";
                else if (viewId == R.id.colorOrange) selectedColor = "#F59E0B";
                else if (viewId == R.id.colorGreen) selectedColor = "#10B981";
                else if (viewId == R.id.colorBlue) selectedColor = "#0EA5E9";
                else if (viewId == R.id.colorPurple) selectedColor = "#8B5CF6";
                else if (viewId == R.id.colorPink) selectedColor = "#EC4899";
            });
            
            // Highlight current
            String hex = "";
            int viewId = colorView.getId();
            if (viewId == R.id.colorRed) hex = "#EF4444";
            else if (viewId == R.id.colorOrange) hex = "#F59E0B";
            else if (viewId == R.id.colorGreen) hex = "#10B981";
            else if (viewId == R.id.colorBlue) hex = "#0EA5E9";
            else if (viewId == R.id.colorPurple) hex = "#8B5CF6";
            else if (viewId == R.id.colorPink) hex = "#EC4899";
            
            if (hex.equalsIgnoreCase(selectedColor)) {
                colorContainer.setBackgroundResource(R.drawable.bg_color_selection_border);
                colorView.setAlpha(1.0f);
            } else {
                colorContainer.setBackground(null);
                colorView.setAlpha(0.3f);
            }
        }

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError("Required");
                return;
            }

            if (category == null) {
                categoryDao.addCategory(new Category(name, selectedColor));
                ToastUtils.showCustomToast(activity, activity.getString(R.string.toast_category_created));
            } else {
                category.setName(name);
                category.setColor(selectedColor);
                categoryDao.updateCategory(category);
                ToastUtils.showCustomToast(activity, activity.getString(R.string.toast_category_updated));
            }
            
            refreshCategories();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showCategoryPopupMenu(Category category, View anchor) {
        View popupView = LayoutInflater.from(activity).inflate(R.layout.layout_category_popup_menu, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        popupView.findViewById(R.id.menuEdit).setOnClickListener(v -> {
            popupWindow.dismiss();
            showCategoryDialog(category);
        });

        popupView.findViewById(R.id.menuDuplicate).setOnClickListener(v -> {
            categoryDao.addCategory(new Category(category.getName() + " (Copy)", category.getColor()));
            refreshCategories();
            popupWindow.dismiss();
            ToastUtils.showCustomToast(activity, activity.getString(R.string.toast_category_duplicated));
        });

        popupView.findViewById(R.id.menuDelete).setOnClickListener(v -> {
            popupWindow.dismiss();
            confirmDeleteCategory(category);
        });

        popupWindow.showAsDropDown(anchor, 100, -50);
    }

    private void confirmDeleteCategory(Category category) {
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_delete_confirmation, null);
        AlertDialog dialog = new AlertDialog.Builder(activity).setView(dialogView).create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView tvMessage = dialogView.findViewById(R.id.dialogMessage);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        tvTitle.setText("Delete Category?");
        tvMessage.setText("Are you sure you want to delete the '" + category.getName() + "' category? This will not delete the tasks within it.");
        btnDelete.setText("Delete");

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v -> {
            categoryDao.deleteCategory(category);
            refreshCategories();
            ToastUtils.showCustomToast(activity, activity.getString(R.string.toast_category_deleted));
            dialog.dismiss();
        });

        dialog.show();
    }

    private void refreshCategories() {
        categoryAdapter.setCategories(categoryDao.getAllCategories());
    }
}
