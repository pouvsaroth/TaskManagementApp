package com.example.taskmanagementapp.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.taskmanagementapp.model.Category;
import com.example.taskmanagementapp.model.Task;

@Database(entities = {Task.class, Category.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract TaskDao taskDao();
    public abstract CategoryDao categoryDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "TaskDB")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // Using for simplicity as per current design
                    .build();
        }
        return instance;
    }
}
