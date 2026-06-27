package com.example.taskmanagementapp.Utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.taskmanagementapp.R;

public class ToastUtils {
    private static Toast currentToast;

    public static void showCustomToast(Context context, String message) {
        showCustomToast(context, message, Toast.LENGTH_SHORT);
    }

    public static void showCustomToast(Context context, String message, int duration) {
        if (currentToast != null) {
            currentToast.cancel();
        }

        View layout = LayoutInflater.from(context).inflate(R.layout.layout_custom_toast, null);
        TextView text = layout.findViewById(R.id.toastMessage);
        text.setText(message);

        currentToast = new Toast(context.getApplicationContext());
        currentToast.setDuration(duration);
        currentToast.setView(layout);
        currentToast.show();
    }
}
