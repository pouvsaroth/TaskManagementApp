package com.example.taskmanagementapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.taskmanagementapp.model.Task;
import java.util.List;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    List<Task> getAllTasks();

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    Task getTaskById(int taskId);

    @Insert
    long addTask(Task task);

    @Update
    void updateTask(Task task);

    @Query("DELETE FROM tasks WHERE id = :taskId")
    void deleteTaskById(int taskId);
    
    @Delete
    void deleteTask(Task task);
}
