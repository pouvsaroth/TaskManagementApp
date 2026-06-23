package com.example.taskmanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanagementapp.adapter.CategoryAdapter;
import com.example.taskmanagementapp.database.AppDatabase;
import com.example.taskmanagementapp.database.CategoryDao;
import com.example.taskmanagementapp.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to manage Categories.
 * Supports Search, Add, Update, and Delete.
 * Accessible from the Sidebar's "+ Add" button.
 */
public class CategoryActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView rvCategories;
    private CategoryAdapter adapter;
    private CategoryDao categoryDao;
    private List<Category> allCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        categoryDao = AppDatabase.getInstance(this).categoryDao();

        // 1. Setup RecyclerView
        rvCategories = findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));

        // 2. Setup Search Functionality
        EditText etSearch = findViewById(R.id.etSearchCategory);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCategories(s.toString());
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        // 3. Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 4. Add New Category Button
        findViewById(R.id.btnAddNewCategory).setOnClickListener(v -> showAddEditDialog(null));

        // 5. Initial Load
        loadCategories();
    }

    /**
     * Loads categories from the database and updates the list.
     */
    public void loadCategories() {
        allCategories = categoryDao.getAllCategories();
        if (adapter == null) {
            adapter = new CategoryAdapter(allCategories, this);
            rvCategories.setAdapter(adapter);
        } else {
            adapter.updateList(allCategories);
        }
    }

    /**
     * Filters the category list based on the search query.
     */
    private void filterCategories(String query) {
        if (query.isEmpty()) {
            adapter.updateList(allCategories);
            return;
        }

        List<Category> filtered = new ArrayList<>();
        for (Category c : allCategories) {
            if (c.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(c);
            }
        }
        adapter.updateList(filtered);
    }

    /**
     * Shows the Create/Edit dialog.
     */
    private void showAddEditDialog(Category category) {
        CreateCategoryDialog dialog = CreateCategoryDialog.newInstance(category);
        dialog.show(getSupportFragmentManager(), "CategoryDialog");
    }

    @Override
    public void onEdit(Category category) {
        showAddEditDialog(category);
    }

    @Override
    public void onDelete(Category category) {
        // Simple delete for now. Could add a confirmation dialog.
        categoryDao.deleteCategory(category);
        loadCategories();
    }
}
