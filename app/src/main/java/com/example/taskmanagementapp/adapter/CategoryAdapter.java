package com.example.taskmanagementapp.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.taskmanagementapp.R;
import com.example.taskmanagementapp.model.Category;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private Map<String, Integer> taskCounts = new HashMap<>();
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
        void onCategoryLongClick(Category category, View view);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    public void setTaskCounts(Map<String, Integer> taskCounts) {
        this.taskCounts = taskCounts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sidebar_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category, taskCounts.getOrDefault(category.getName(), 0), listener);
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        View colorDot;
        TextView tvName, tvCount;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            colorDot = itemView.findViewById(R.id.categoryColorDot);
            tvName = itemView.findViewById(R.id.categoryName);
            tvCount = itemView.findViewById(R.id.taskCount);
        }

        public void bind(Category category, int count, OnCategoryClickListener listener) {
            tvName.setText(category.getName());
            String countText = itemView.getContext().getString(
                    count == 1 ? R.string.task_count_singular : R.string.task_count_plural, count);
            tvCount.setText(countText);
            try {
                colorDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(category.getColor())));
            } catch (Exception e) {
                colorDot.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            }

            itemView.setOnClickListener(v -> listener.onCategoryClick(category));
            itemView.setOnLongClickListener(v -> {
                listener.onCategoryLongClick(category, v);
                return true;
            });
        }
    }
}
