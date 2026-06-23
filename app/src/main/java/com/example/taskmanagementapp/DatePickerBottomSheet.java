package com.example.taskmanagementapp;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.example.taskmanagementapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatePickerBottomSheet extends BottomSheetDialogFragment {

    public interface OnDateSelectedListener {
        void onDateSelected(Date date);
    }

    private OnDateSelectedListener listener;
    private Calendar calendar;
    private TextView tvMonthYear;
    private CalendarAdapter adapter;
    private Date selectedDate;

    public static DatePickerBottomSheet newInstance(Date initialDate, OnDateSelectedListener listener) {
        DatePickerBottomSheet fragment = new DatePickerBottomSheet();
        fragment.selectedDate = initialDate;
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Theme_TaskManagementApp_BottomSheetDialog);
        calendar = Calendar.getInstance();
        if (selectedDate != null) {
            calendar.setTime(selectedDate);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_date_picker_bottom_sheet, container, false);

        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        GridView gridView = view.findViewById(R.id.calendarGrid);

        adapter = new CalendarAdapter(getContext(), calendar, selectedDate);
        gridView.setAdapter(adapter);

        updateMonthYear();

        view.findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        view.findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            Date clickedDate = (Date) adapter.getItem(position);
            if (clickedDate != null) {
                selectedDate = clickedDate;
                adapter.setSelectedDate(selectedDate);
            }
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnDone).setOnClickListener(v -> {
            if (listener != null && selectedDate != null) {
                listener.onDateSelected(selectedDate);
            }
            dismiss();
        });

        return view;
    }

    private void updateMonthYear() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
        tvMonthYear.setText(sdf.format(calendar.getTime()));
    }

    private void updateCalendar() {
        updateMonthYear();
        adapter.updateCalendar(calendar);
    }

    private static class CalendarAdapter extends BaseAdapter {
        private Context context;
        private Calendar calendar;
        private List<Date> days = new ArrayList<>();
        private Date selectedDate;
        private int currentMonth;

        public CalendarAdapter(Context context, Calendar calendar, Date selectedDate) {
            this.context = context;
            this.calendar = (Calendar) calendar.clone();
            this.selectedDate = selectedDate;
            this.currentMonth = calendar.get(Calendar.MONTH);
            generateDays();
        }

        public void updateCalendar(Calendar newCalendar) {
            this.calendar = (Calendar) newCalendar.clone();
            this.currentMonth = calendar.get(Calendar.MONTH);
            generateDays();
            notifyDataSetChanged();
        }

        public void setSelectedDate(Date selectedDate) {
            this.selectedDate = selectedDate;
            notifyDataSetChanged();
        }

        private void generateDays() {
            days.clear();
            Calendar temp = (Calendar) calendar.clone();
            temp.set(Calendar.DAY_OF_MONTH, 1);
            
            // Get the first day of the week for the first day of the month
            int firstDayOfWeek = temp.get(Calendar.DAY_OF_WEEK) - 1;
            temp.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek);

            // Generate 6 weeks of days (42 days)
            for (int i = 0; i < 42; i++) {
                days.add(temp.getTime());
                temp.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        @Override
        public int getCount() {
            return days.size();
        }

        @Override
        public Object getItem(int position) {
            return days.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
            }

            TextView tvDay = convertView.findViewById(R.id.tvDay);
            Date date = days.get(position);
            Calendar c = Calendar.getInstance();
            c.setTime(date);

            tvDay.setText(String.valueOf(c.get(Calendar.DAY_OF_MONTH)));

            // Style for different months
            if (c.get(Calendar.MONTH) != currentMonth) {
                tvDay.setTextColor(ContextCompat.getColor(context, R.color.text_muted));
                tvDay.setAlpha(0.5f);
            } else {
                tvDay.setTextColor(Color.BLACK);
                tvDay.setAlpha(1.0f);
            }

            // Style for selected date
            if (selectedDate != null) {
                Calendar s = Calendar.getInstance();
                s.setTime(selectedDate);
                if (c.get(Calendar.YEAR) == s.get(Calendar.YEAR) &&
                    c.get(Calendar.MONTH) == s.get(Calendar.MONTH) &&
                    c.get(Calendar.DAY_OF_MONTH) == s.get(Calendar.DAY_OF_MONTH)) {
                    
                    tvDay.setBackgroundResource(R.drawable.bg_calendar_day_selected);
                    tvDay.setTextColor(Color.WHITE);
                } else {
                    tvDay.setBackground(null);
                }
            } else {
                tvDay.setBackground(null);
            }

            return convertView;
        }
    }
}
