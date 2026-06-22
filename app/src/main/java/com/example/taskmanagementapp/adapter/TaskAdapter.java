package com.example.taskmanagementapp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.taskmanagementapp.R;
import com.example.taskmanagementapp.model.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private OnTaskClickListener listener;
    private boolean globalReminderEnabled = true;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskEdit(Task task);
        void onTaskDelete(Task task);
        void onTaskDuplicate(Task task);
        void onTaskStatusChanged(Task task);
        void onTaskReminderToggled(Task task);
    }

    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    public void setGlobalReminderEnabled(boolean enabled) {
        this.globalReminderEnabled = enabled;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        
        // Initial check for global reminder setting
        SharedPreferences prefs = parent.getContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        globalReminderEnabled = prefs.getBoolean("isReminderEnabled", true);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task, listener, globalReminderEnabled);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView tvTitle, tvDate, tagPriority, tagCategory;
        ImageView menuIcon, ivCalendarIcon, ivReminderIcon;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.taskCheckbox);
            tvTitle = itemView.findViewById(R.id.taskTitle);
            tvDate = itemView.findViewById(R.id.taskDate);
            tagPriority = itemView.findViewById(R.id.tagHigh);
            tagCategory = itemView.findViewById(R.id.tagWork);
            menuIcon = itemView.findViewById(R.id.menuIcon);
            ivCalendarIcon = itemView.findViewById(R.id.ivCalendarIcon);
            ivReminderIcon = itemView.findViewById(R.id.ivReminderIcon);
        }

        public void bind(Task task, OnTaskClickListener listener, boolean globalReminderEnabled) {
            tvTitle.setText(task.getTitle());
            tvDate.setText(task.getDueDate());
            tagPriority.setText(task.getPriority());
            tagCategory.setText(task.getCategory());
            
            if (globalReminderEnabled) {
                ivReminderIcon.setVisibility(View.VISIBLE);
                if (task.isReminderEnabled()) {
                    ivReminderIcon.setImageTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.icon_green)));
                    ivReminderIcon.setAlpha(1.0f);
                } else {
                    ivReminderIcon.setImageTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.text_muted)));
                    ivReminderIcon.setAlpha(0.4f);
                }
            } else {
                ivReminderIcon.setVisibility(View.GONE);
            }

            ivReminderIcon.setOnClickListener(v -> {
                task.setReminderEnabled(!task.isReminderEnabled());
                listener.onTaskReminderToggled(task);
                // Update UI immediately
                if (task.isReminderEnabled()) {
                    ivReminderIcon.setImageTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.icon_green)));
                    ivReminderIcon.setAlpha(1.0f);
                } else {
                    ivReminderIcon.setImageTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.text_muted)));
                    ivReminderIcon.setAlpha(0.4f);
                }
            });
            
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(task.isCompleted());

            if (task.isCompleted()) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                itemView.setAlpha(0.5f);
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                itemView.setAlpha(1.0f);
            }

            checkOverdue(task);

            if ("High".equals(task.getPriority())) {
                tagPriority.setTextColor(Color.parseColor("#FF6B35"));
                tagPriority.setBackgroundResource(R.color.bg_icon_red_dark);
            } else if ("Medium".equals(task.getPriority())) {
                tagPriority.setTextColor(Color.parseColor("#F59E0B"));
                tagPriority.setBackgroundResource(R.color.bg_icon_orange_dark);
            } else {
                tagPriority.setTextColor(Color.parseColor("#6B7280"));
                tagPriority.setBackgroundResource(R.color.bg_icon_blue_dark);
            }

            itemView.setOnClickListener(v -> listener.onTaskClick(task));
            
            menuIcon.setOnClickListener(v -> {
                View popupView = LayoutInflater.from(v.getContext()).inflate(R.layout.layout_task_popup_menu, null);
                
                PopupWindow popupWindow = new PopupWindow(popupView, 
                    ViewGroup.LayoutParams.WRAP_CONTENT, 
                    ViewGroup.LayoutParams.WRAP_CONTENT, 
                    true);
                
                // Ensure no default background is interfering
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                popupWindow.setElevation(0); // Use only CardView shadow
                
                popupView.findViewById(R.id.menuEdit).setOnClickListener(v1 -> {
                    listener.onTaskEdit(task);
                    popupWindow.dismiss();
                });
                
                popupView.findViewById(R.id.menuDuplicate).setOnClickListener(v1 -> {
                    listener.onTaskDuplicate(task);
                    popupWindow.dismiss();
                });
                
                popupView.findViewById(R.id.menuDelete).setOnClickListener(v1 -> {
                    listener.onTaskDelete(task);
                    popupWindow.dismiss();
                });

                // Correctly offset for CardView shadow padding (added by UseCompatPadding)
                popupWindow.showAsDropDown(v, -380, -20);
            });

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.setCompleted(isChecked);
                if (isChecked) {
                    tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    itemView.setAlpha(0.5f);
                } else {
                    tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    itemView.setAlpha(1.0f);
                }
                checkOverdue(task);
                listener.onTaskStatusChanged(task);
            });
        }

        private void checkOverdue(Task task) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
                Date taskDate = sdf.parse(task.getDueDate());
                
                Calendar calToday = Calendar.getInstance();
                calToday.set(Calendar.HOUR_OF_DAY, 0);
                calToday.set(Calendar.MINUTE, 0);
                calToday.set(Calendar.SECOND, 0);
                calToday.set(Calendar.MILLISECOND, 0);
                Date today = calToday.getTime();

                if (taskDate != null && taskDate.before(today) && !task.isCompleted()) {
                    tvDate.setTextColor(Color.parseColor("#EF4444"));
                    ivCalendarIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#EF4444")));
                } else {
                    tvDate.setTextColor(itemView.getContext().getResources().getColor(R.color.text_muted));
                    ivCalendarIcon.setImageTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.text_muted)));
                }
            } catch (Exception e) {
                tvDate.setTextColor(itemView.getContext().getResources().getColor(R.color.text_muted));
                ivCalendarIcon.setImageTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.text_muted)));
            }
        }
    }
}
