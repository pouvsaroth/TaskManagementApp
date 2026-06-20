package com.example.taskmanagementapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "tasks")
public class Task implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String description;
    private String dueDate;
    private String priority;
    private String category;
    private boolean isCompleted;
    private boolean reminderEnabled;

    public Task() {}

    public Task(int id, String title, String description, String dueDate, String priority, String category, boolean isCompleted) {
        this(id, title, description, dueDate, priority, category, isCompleted, false);
    }

    public Task(int id, String title, String description, String dueDate, String priority, String category, boolean isCompleted, boolean reminderEnabled) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.category = category;
        this.isCompleted = isCompleted;
        this.reminderEnabled = reminderEnabled;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public boolean isReminderEnabled() { return reminderEnabled; }
    public void setReminderEnabled(boolean reminderEnabled) { this.reminderEnabled = reminderEnabled; }
}
