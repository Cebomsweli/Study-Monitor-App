package com.example.studytrackerapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class BootBroadcastReceiver extends BroadcastReceiver {

    private static final String PREFS_NAME = "StudyTrackerPrefs";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean remindersEnabled = prefs.getBoolean("reminders_enabled", false);

            if (!remindersEnabled) return;

            int hour = prefs.getInt("reminder_hour", 8);
            int minute = prefs.getInt("reminder_minute", 0);
            String message = prefs.getString("reminder_message", "Time to study!");

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            Intent alarmIntent = new Intent(context, ReminderReceiver.class);
            alarmIntent.putExtra("message", message);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    Intent settingsIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(settingsIntent);
                    return;
                }

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );

                Log.d("BootReceiver", "Reminder rescheduled at: " + calendar.getTime());
            }
        }
    }
}
