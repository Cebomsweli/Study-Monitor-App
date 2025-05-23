package com.example.studytrackerapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    private SwitchCompat reminderSwitch;
    private View reminderOptions;
    private MaterialButton reminderTimeButton;
    private MaterialButton selectDaysButton;
    private TextInputEditText reminderMessage;
    private TextInputEditText dailyGoalInput;
    private MaterialButton saveSettingsButton;

    private int selectedHour = 8;
    private int selectedMinute = 0;
    private Set<String> selectedDays = new HashSet<>();

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "StudyTrackerPrefs";

    private static final String KEY_REMINDERS_ENABLED = "reminders_enabled";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "reminder_minute";
    private static final String KEY_REMINDER_DAYS = "reminder_days";
    private static final String KEY_REMINDER_MESSAGE = "reminder_message";
    private static final String KEY_DAILY_GOAL = "daily_goal";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        reminderSwitch = view.findViewById(R.id.reminderSwitch);
        reminderOptions = view.findViewById(R.id.reminderOptions);
        reminderTimeButton = view.findViewById(R.id.reminderTimeButton);
        selectDaysButton = view.findViewById(R.id.selectDaysButton);
        reminderMessage = view.findViewById(R.id.reminderMessage);
        dailyGoalInput = view.findViewById(R.id.dailyGoalInput);
        saveSettingsButton = view.findViewById(R.id.saveSettingsButton);

        loadPreferences();
        setupListeners();

        return view;
    }

    private void loadPreferences() {
        boolean remindersEnabled = sharedPreferences.getBoolean(KEY_REMINDERS_ENABLED, false);
        selectedHour = sharedPreferences.getInt(KEY_REMINDER_HOUR, 8);
        selectedMinute = sharedPreferences.getInt(KEY_REMINDER_MINUTE, 0);
        selectedDays = sharedPreferences.getStringSet(KEY_REMINDER_DAYS, new HashSet<>());
        String reminderText = sharedPreferences.getString(KEY_REMINDER_MESSAGE, "Time to study!");
        int dailyGoal = sharedPreferences.getInt(KEY_DAILY_GOAL, 120);

        reminderSwitch.setChecked(remindersEnabled);
        reminderOptions.setVisibility(remindersEnabled ? View.VISIBLE : View.GONE);
        updateTimeButtonText();
        reminderMessage.setText(reminderText);
        dailyGoalInput.setText(String.valueOf(dailyGoal));
        updateDaysButtonText();
    }

    private void setupListeners() {
        reminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            reminderOptions.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) cancelPreviousReminders();
        });

        reminderTimeButton.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    requireContext(),
                    (TimePicker view, int hourOfDay, int minute) -> {
                        selectedHour = hourOfDay;
                        selectedMinute = minute;
                        updateTimeButtonText();
                    },
                    selectedHour, selectedMinute, true
            );
            timePickerDialog.show();
        });

        selectDaysButton.setOnClickListener(v -> {
            final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            boolean[] checkedItems = new boolean[days.length];
            for (int i = 0; i < days.length; i++) {
                checkedItems[i] = selectedDays.contains(days[i]);
            }

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Select Repeat Days")
                    .setMultiChoiceItems(days, checkedItems, (dialog, which, isChecked) -> {
                        if (isChecked) {
                            selectedDays.add(days[which]);
                        } else {
                            selectedDays.remove(days[which]);
                        }
                    })
                    .setPositiveButton("OK", (dialog, which) -> updateDaysButtonText())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        saveSettingsButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_REMINDERS_ENABLED, reminderSwitch.isChecked());
            editor.putInt(KEY_REMINDER_HOUR, selectedHour);
            editor.putInt(KEY_REMINDER_MINUTE, selectedMinute);
            editor.putStringSet(KEY_REMINDER_DAYS, selectedDays);
            editor.putString(KEY_REMINDER_MESSAGE, reminderMessage.getText().toString());

            String goalText = dailyGoalInput.getText().toString();
            int goal = goalText.isEmpty() ? 0 : Integer.parseInt(goalText);
            editor.putInt(KEY_DAILY_GOAL, goal);
            editor.apply();

            if (reminderSwitch.isChecked()) {
                scheduleReminder();
            }

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Saved")
                    .setMessage("Settings have been saved.")
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    private void scheduleReminder() {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Permission Needed")
                    .setMessage("Please allow exact alarms to enable reminders.")
                    .setPositiveButton("Allow", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }

        cancelPreviousReminders();

        for (String day : selectedDays) {
            int dayOfWeek = convertDayToCalendarConstant(day);
            if (dayOfWeek == -1) continue;

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
            calendar.set(Calendar.MINUTE, selectedMinute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
            }

            Intent intent = new Intent(requireContext(), ReminderReceiver.class);
            intent.putExtra("message", Objects.requireNonNull(reminderMessage.getText()).toString());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    requireContext(), dayOfWeek, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );

            Log.d(TAG, "Reminder scheduled for " + day + " at " + calendar.getTime());
        }
    }

    private void cancelPreviousReminders() {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        for (String day : new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}) {
            int dayOfWeek = convertDayToCalendarConstant(day);
            if (dayOfWeek == -1) continue;

            Intent intent = new Intent(requireContext(), ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    requireContext(), dayOfWeek, intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
    }

    private int convertDayToCalendarConstant(String day) {
        switch (day) {
            case "Sun": return Calendar.SUNDAY;
            case "Mon": return Calendar.MONDAY;
            case "Tue": return Calendar.TUESDAY;
            case "Wed": return Calendar.WEDNESDAY;
            case "Thu": return Calendar.THURSDAY;
            case "Fri": return Calendar.FRIDAY;
            case "Sat": return Calendar.SATURDAY;
            default: return -1;
        }
    }

    private void updateTimeButtonText() {
        String timeText = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
        reminderTimeButton.setText(timeText);
    }

    private void updateDaysButtonText() {
        if (selectedDays.isEmpty()) {
            selectDaysButton.setText("Select Days");
        } else {
            selectDaysButton.setText(String.join(", ", selectedDays));
        }
    }
}
