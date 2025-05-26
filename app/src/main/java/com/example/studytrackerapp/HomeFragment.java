package com.example.studytrackerapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment {

    private TextView tvWelcome, tvUserEmail, tvDailyQuote, tvQuoteAuthor;
    private MaterialTextView todayHours, weekAverage, tvPeakTime, tvFocusScore;
    private FirebaseAuth auth;

    // Quotes
    private final List<Quote> quotes = Arrays.asList(
            new Quote("The expert in anything was once a beginner.", "Helen Hayes"),
            new Quote("Success is the sum of small efforts, repeated day in and day out.", "Robert Collier"),
            new Quote("The only way to learn mathematics is to do mathematics.", "Paul Halmos"),
            new Quote("Don't watch the clock; do what it does. Keep going.", "Sam Levenson"),
            new Quote("The future belongs to those who believe in the beauty of their dreams.", "Eleanor Roosevelt")
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        auth = FirebaseAuth.getInstance();

        // Initialize UI elements
        tvWelcome = view.findViewById(R.id.tv_welcome);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvDailyQuote = view.findViewById(R.id.tv_daily_quote);
        tvQuoteAuthor = view.findViewById(R.id.tv_quote_author);
        todayHours = view.findViewById(R.id.tv_today_study);
        weekAverage = view.findViewById(R.id.tv_weekly_avg);
        //daysStudied = view.findViewById(R.id.tv_days_studied);
        tvPeakTime = view.findViewById(R.id.tv_peak_time);
        tvFocusScore = view.findViewById(R.id.tv_focus_score);

        // Button listeners
        view.findViewById(R.id.btn_start_session).setOnClickListener(v -> startSession());
        view.findViewById(R.id.btn_view_stats).setOnClickListener(v -> viewStats());

        displayUserInfo();
        showRandomQuote();
        loadStudyDataFromFirestore();

        return view;
    }

    private void displayUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String displayName = user.getDisplayName();

            if (displayName != null && !displayName.isEmpty()) {
                tvWelcome.setText("Welcome back, " + displayName + "!");
            } else {
                tvWelcome.setText("Welcome back!");
            }

            tvUserEmail.setText(email);
        }
    }

    private void showRandomQuote() {
        Random random = new Random();
        Quote quote = quotes.get(random.nextInt(quotes.size()));
        tvDailyQuote.setText(quote.getText());
        tvQuoteAuthor.setText("- " + quote.getAuthor());
    }

    private void loadStudyDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference sessionsRef = db.collection("Students");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long weekStartMillis = calendar.getTimeInMillis();
        long now = System.currentTimeMillis();

        sessionsRef
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    float[] dailyTotals = new float[7];
                    int[] hourlyStudyCounts = new int[24];
                    float totalFocusTime = 0f;
                    int sessionCount = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Timestamp dateSaved = doc.getTimestamp("dateSaved");
                        if (dateSaved == null) continue;

                        long timeMillis = dateSaved.toDate().getTime();
                        if (timeMillis < weekStartMillis || timeMillis > now) continue;

                        float focusTime = doc.getDouble("focusTime") != null ?
                                doc.getDouble("focusTime").floatValue() : 0f;

                        // Daily totals
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(timeMillis);
                        int index = cal.get(Calendar.DAY_OF_WEEK) - 1;
                        dailyTotals[index] += focusTime;

                        // Peak hours
                        Timestamp startTime = doc.getTimestamp("startTime");
                        if (startTime != null) {
                            Calendar startCal = Calendar.getInstance();
                            startCal.setTime(startTime.toDate());
                            int hour = startCal.get(Calendar.HOUR_OF_DAY);
                            hourlyStudyCounts[hour]++;
                        }

                        totalFocusTime += focusTime;
                        sessionCount++;
                    }

                    updateSummaryStats(dailyTotals);
                    updatePeakStudyTime(hourlyStudyCounts);
                    updateFocusScore(totalFocusTime, sessionCount);
                })
                .addOnFailureListener(e -> {
                    // Handle failure if needed
                });
    }

    private void updateSummaryStats(float[] dailyTotals) {
        Calendar calendar = Calendar.getInstance();
        int todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        float todayHoursValue = dailyTotals[todayIndex];

        int daysStudiedCount = 0;
        int daysSoFar = todayIndex + 1;
        float totalHoursSoFar = 0f;

        for (int i = 0; i < daysSoFar; i++) {
            if (dailyTotals[i] > 0) daysStudiedCount++;
            totalHoursSoFar += dailyTotals[i];
        }

        float weekAverageValue = daysSoFar > 0 ? totalHoursSoFar / daysSoFar : 0f;

        todayHours.setText(String.format(" %.2fh", todayHoursValue));
        weekAverage.setText(String.format(" %.2fh/day", weekAverageValue));
        //daysStudied.setText(String.format(" %d days", daysStudiedCount));
    }

    private void updatePeakStudyTime(int[] hourlyStudyCounts) {
        int peakHour = 0;
        int maxSessions = 0;
        for (int i = 0; i < hourlyStudyCounts.length; i++) {
            if (hourlyStudyCounts[i] > maxSessions) {
                maxSessions = hourlyStudyCounts[i];
                peakHour = i;
            }
        }

        String peakTime = String.format("%02d:00 - %02d:00", peakHour, (peakHour + 2) % 24);
        tvPeakTime.setText(peakTime);
    }

    private void updateFocusScore(float totalFocusTime, int sessionCount) {
        if (sessionCount == 0) {
            tvFocusScore.setText("0%");
            return;
        }
        float average = (totalFocusTime / sessionCount) * 100f;
        int rounded = Math.min(100, Math.round(average));
        tvFocusScore.setText(rounded + "%");
    }

    private void startSession() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).showToast("Starting study session...");
        }
    }

    private void viewStats() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).showToast("Viewing statistics...");
        }
    }

    private static class Quote {
        private final String text;
        private final String author;

        public Quote(String text, String author) {
            this.text = text;
            this.author = author;
        }

        public String getText() { return text; }

        public String getAuthor() { return author; }
    }
}
