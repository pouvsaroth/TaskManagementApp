package com.example.taskmanagementapp.Utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.taskmanagementapp.database.AppDatabase;
import com.example.taskmanagementapp.database.TaskDao;
import com.example.taskmanagementapp.model.Task;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            TaskDao taskDao = AppDatabase.getInstance(context).taskDao();
            List<Task> tasks = taskDao.getAllTasks();
            for (Task task : tasks) {
                if (task.isReminderEnabled() && !task.isCompleted()) {
                    ReminderManager.setReminder(context, task);
                }
            }
        }
    }
}
