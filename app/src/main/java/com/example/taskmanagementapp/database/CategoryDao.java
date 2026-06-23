package com.example.taskmanagementapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.taskmanagementapp.model.Category;
import java.util.List;

@Dao
public interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY id ASC")
    List<Category> getAllCategories();

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    Category getCategoryByName(String name);

    @Insert
    long addCategory(Category category);

    @Update
    void updateCategory(Category category);

    @Delete
    void deleteCategory(Category category);
}
