package com.example.studytrackerapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SessionFragment extends Fragment {

    private TextView timerTextView;
    private MaterialButton startButton, stopButton, saveButton;
    private MaterialAutoCompleteTextView goalSpinner;
    private TextInputEditText subjectEditText, notesEditText;
    private LinearProgressIndicator progressBar;

    private long startTime = 0;
    private long endTime = 0;
    private int goalHours = 1;

    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    private FirebaseFirestore firestore;
    private String userId;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "StudyPrefs";
    private static final String KEY_START_TIME = "startTime";
    private static final String KEY_GOAL_HOURS = "goalHours";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.session_fragment, container, false);

        // UI References
        timerTextView = view.findViewById(R.id.timerTextView);
        startButton = view.findViewById(R.id.startButton);
        stopButton = view.findViewById(R.id.stopButton);
        goalSpinner = view.findViewById(R.id.goalSpinner);
        subjectEditText = view.findViewById(R.id.subjectEditText);
        notesEditText = view.findViewById(R.id.notesEditText);
        saveButton = view.findViewById(R.id.saveButton);
        progressBar = view.findViewById(R.id.progressBar);

        // Firebase
        firestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user != null ? user.getUid() : "anonymous";

        // SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        startTime = sharedPreferences.getLong(KEY_START_TIME, 0);
        goalHours = sharedPreferences.getInt(KEY_GOAL_HOURS, 1);

        // Dropdown Setup
        String[] goals = {"1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "6 Hours", "7 Hours"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, goals);
        goalSpinner.setAdapter(adapter);
        goalSpinner.setInputType(0); // Disable keyboard

        goalSpinner.setOnClickListener(v -> goalSpinner.showDropDown());
        goalSpinner.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) goalSpinner.showDropDown();
        });

        goalSpinner.setOnItemClickListener((parent, view1, position, id) -> {
            goalHours = position + 1;
            progressBar.setMax(goalHours * 3600);
        });

        // Timer logic
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (startTime > 0) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    long seconds = (elapsed / 1000) % 60;
                    long minutes = (elapsed / (1000 * 60)) % 60;
                    long hours = (elapsed / (1000 * 60 * 60));
                    String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                    timerTextView.setText(time);

                    int elapsedSeconds = (int) (elapsed / 1000);
                    int maxSeconds = goalHours * 3600;
                    progressBar.setProgress(Math.min(elapsedSeconds, maxSeconds));
                    progressBar.setVisibility(View.VISIBLE);

                    timerHandler.postDelayed(this, 1000);
                }
            }
        };

        // Restore running session if any
        if (startTime > 0) {
            progressBar.setMax(goalHours * 3600);
            timerHandler.post(timerRunnable);
            progressBar.setVisibility(View.VISIBLE);
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        }

        // Start session
        startButton.setOnClickListener(v -> {
            if (goalSpinner.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Please select a study goal", Toast.LENGTH_SHORT).show();
                return;
            }
            startTime = System.currentTimeMillis();
            endTime = 0;
            progressBar.setMax(goalHours * 3600);
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
            timerHandler.post(timerRunnable);
            startButton.setEnabled(false);
            stopButton.setEnabled(true);

            // Save to SharedPreferences
            sharedPreferences.edit()
                    .putLong(KEY_START_TIME, startTime)
                    .putInt(KEY_GOAL_HOURS, goalHours)
                    .apply();
        });

        // Stop session
        stopButton.setOnClickListener(v -> {
            timerHandler.removeCallbacks(timerRunnable);
            endTime = System.currentTimeMillis();
            stopButton.setEnabled(false);
            startButton.setEnabled(true);

            sharedPreferences.edit()
                    .remove(KEY_START_TIME)
                    .remove(KEY_GOAL_HOURS)
                    .apply();
        });

        // Save session
        saveButton.setOnClickListener(v -> saveSession());

        // Initial state
        if (startTime == 0) {
            stopButton.setEnabled(false);
            progressBar.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    private void saveSession() {
        String subject = subjectEditText.getText() != null ? subjectEditText.getText().toString().trim() : "";
        String notes = notesEditText.getText() != null ? notesEditText.getText().toString().trim() : "";
        String goalText = goalSpinner.getText().toString().trim();

        if (subject.isEmpty()) {
            subjectEditText.setError("Subject is required");
            return;
        }

        try {
            goalHours = Integer.parseInt(goalText.split(" ")[0]);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Please select a valid goal", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startTime > 0 && (endTime == 0 || endTime < startTime)) {
            endTime = System.currentTimeMillis();
        }

        long focusDurationMillis = 0;
        if (startTime > 0 && endTime > startTime) {
            focusDurationMillis = endTime - startTime;
        }

        double focusHours = focusDurationMillis / (1000.0 * 60 * 60);

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("userId", userId);
        sessionData.put("startTime", new Timestamp(new Date(startTime)));
        sessionData.put("endTime", new Timestamp(new Date(endTime)));
        sessionData.put("goal", goalHours);
        sessionData.put("subject", subject);
        sessionData.put("notes", notes);
        sessionData.put("dateSaved", Timestamp.now());
        sessionData.put("focusTime", focusHours);

        firestore.collection("Students")
                .add(sessionData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Session saved", Toast.LENGTH_SHORT).show();
                    resetForm();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save session", Toast.LENGTH_SHORT).show();
                });
    }

    private void resetForm() {
        timerHandler.removeCallbacks(timerRunnable);
        timerTextView.setText("00:00:00");
        startTime = 0;
        endTime = 0;
        subjectEditText.setText("");
        notesEditText.setText("");
        //goalSpinner.setText("");
        goalHours = 1;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.INVISIBLE);

        sharedPreferences.edit()
                .remove(KEY_START_TIME)
                .remove(KEY_GOAL_HOURS)
                .apply();
    }
}
