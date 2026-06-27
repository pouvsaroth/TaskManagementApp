package com.example.taskmanagementapp.Utilities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import com.example.taskmanagementapp.model.Task;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderManager {

    public static void setReminder(Context context, Task task) {
        if (!task.isReminderEnabled() || task.isCompleted()) {
            cancelReminder(context, task.getId());
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        String timeToUse = (task.getDueTime() != null && !task.getDueTime().isEmpty()) 
                ? task.getDueTime() 
                : prefs.getString("selectedTime", "9:00 AM");

        try {
            SimpleDateFormat fullSdf = new SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.ENGLISH);
            String fullDateStr = task.getDueDate() + " " + timeToUse;
            Date reminderDate = fullSdf.parse(fullDateStr);

            if (reminderDate != null && reminderDate.after(new Date())) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(context, ReminderReceiver.class);
                intent.putExtra("task_title", task.getTitle());
                intent.putExtra("task_id", task.getId());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, task.getId(), intent, 
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderDate.getTime(), pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderDate.getTime(), pendingIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cancelReminder(Context context, int taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, taskId, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }
}
