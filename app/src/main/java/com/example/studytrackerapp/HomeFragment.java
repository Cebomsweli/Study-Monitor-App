package com.example.studytrackerapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment {

    private TextView tvWelcome, tvUserEmail, tvDailyQuote, tvQuoteAuthor;
    private TextView tvTodayStudy, tvWeeklyAvg;
    private FirebaseAuth auth;

    // Quotes data structure
    private final List<Quote> quotes = Arrays.asList(
            new Quote("The expert in anything was once a beginner.", "Helen Hayes"),
            new Quote("Success is the sum of small efforts, repeated day in and day out.", "Robert Collier"),
            new Quote("The only way to learn mathematics is to do mathematics.", "Paul Halmos"),
            new Quote("Don't watch the clock; do what it does. Keep going.", "Sam Levenson"),
            new Quote("The future belongs to those who believe in the beauty of their dreams.", "Eleanor Roosevelt")
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase Auth only
        auth = FirebaseAuth.getInstance();

        // Initialize views
        tvWelcome = view.findViewById(R.id.tv_welcome);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvDailyQuote = view.findViewById(R.id.tv_daily_quote);
        tvQuoteAuthor = view.findViewById(R.id.tv_quote_author);
        tvTodayStudy = view.findViewById(R.id.tv_today_study);
        tvWeeklyAvg = view.findViewById(R.id.tv_weekly_avg);

        // Set button click listeners
        view.findViewById(R.id.btn_start_session).setOnClickListener(v -> startSession());
        view.findViewById(R.id.btn_view_stats).setOnClickListener(v -> viewStats());

        // Load user data and display static content
        displayUserInfo();
        showRandomQuote();
        displaySampleStats();

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
        Quote randomQuote = quotes.get(random.nextInt(quotes.size()));
        tvDailyQuote.setText(randomQuote.getText());
        tvQuoteAuthor.setText("- " + randomQuote.getAuthor());
    }

    private void displaySampleStats() {
        // Display sample static data
        tvTodayStudy.setText("2h 45m");
        tvWeeklyAvg.setText("3.2h/day");
    }

    private void startSession() {
        // Placeholder for session start
        if (getActivity() != null) {
            ((MainActivity) getActivity()).showToast("Starting study session...");
        }
    }

    private void viewStats() {
        // Placeholder for stats view
        if (getActivity() != null) {
            ((MainActivity) getActivity()).showToast("Viewing statistics...");
        }
    }

    // Helper class for quotes
    private static class Quote {
        private final String text;
        private final String author;

        public Quote(String text, String author) {
            this.text = text;
            this.author = author;
        }

        public String getText() {
            return text;
        }

        public String getAuthor() {
            return author;
        }
    }
}