package com.example.taskmanagementapp;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.taskmanagementapp.database.AppDatabase;
import com.example.taskmanagementapp.model.Category;

public class CreateCategoryDialog extends DialogFragment {

    private EditText etCategoryName;
    private TextView dialogTitle, btnCreate;
    private String selectedColor = "#EF4444"; // Default red
    private Category existingCategory;
    private View[] colorViews;
    private final String[] colors = {"#EF4444", "#F59E0B", "#10B981", "#0EA5E9", "#8B5CF6", "#EC4899"};

    public static CreateCategoryDialog newInstance(Category category) {
        CreateCategoryDialog frag = new CreateCategoryDialog();
        Bundle args = new Bundle();
        args.putSerializable("category", category);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            existingCategory = (Category) getArguments().getSerializable("category");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_create_category, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialogTitle = view.findViewById(R.id.dialogTitle);
        etCategoryName = view.findViewById(R.id.etCategoryName);
        btnCreate = view.findViewById(R.id.btnCreate);

        setupColorSelection(view);

        if (existingCategory != null) {
            dialogTitle.setText("Edit Category");
            btnCreate.setText("Update");
            etCategoryName.setText(existingCategory.getName());
            selectedColor = existingCategory.getColor();
            highlightSelectedColor();
        }

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
        btnCreate.setOnClickListener(v -> saveCategory());

        return view;
    }

    private void setupColorSelection(View view) {
        colorViews = new View[]{
                view.findViewById(R.id.colorRed), view.findViewById(R.id.colorOrange),
                view.findViewById(R.id.colorGreen), view.findViewById(R.id.colorBlue),
                view.findViewById(R.id.colorPurple), view.findViewById(R.id.colorPink)
        };

        for (int i = 0; i < colorViews.length; i++) {
            final int index = i;
            if (colorViews[i] != null) {
                colorViews[i].setOnClickListener(v -> {
                    selectedColor = colors[index];
                    highlightSelectedColor();
                });
            }
        }
        highlightSelectedColor();
    }

    private void highlightSelectedColor() {
        int strokeWidth = (int) (3 * getResources().getDisplayMetrics().density);
        int strokeColor = ContextCompat.getColor(requireContext(), R.color.figma_selection_border);

        for (int i = 0; i < colorViews.length; i++) {
            if (colorViews[i] != null) {
                GradientDrawable bg = (GradientDrawable) colorViews[i].getBackground().mutate();
                if (colors[i].equalsIgnoreCase(selectedColor)) {
                    bg.setStroke(strokeWidth, strokeColor);
                    colorViews[i].setScaleX(1.1f);
                    colorViews[i].setScaleY(1.1f);
                } else {
                    bg.setStroke(0, Color.TRANSPARENT);
                    colorViews[i].setScaleX(1.0f);
                    colorViews[i].setScaleY(1.0f);
                }
            }
        }
    }

    private void saveCategory() {
        String name = etCategoryName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a category name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (existingCategory != null) {
            existingCategory.setName(name);
            existingCategory.setColor(selectedColor);
            AppDatabase.getInstance(getContext()).categoryDao().updateCategory(existingCategory);
        } else {
            Category category = new Category(name, selectedColor);
            AppDatabase.getInstance(getContext()).categoryDao().addCategory(category);
        }

        if (getActivity() instanceof CategoryActivity) {
            ((CategoryActivity) getActivity()).loadCategories();
        }

        dismiss();
    }
}