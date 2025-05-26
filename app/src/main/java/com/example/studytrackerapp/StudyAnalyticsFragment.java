package com.example.studytrackerapp;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudyAnalyticsFragment extends Fragment {

    private BarChart weeklyBarChart;
    private PieChart subjectPieChart;
    private MaterialTextView goalValue;

    // New TextViews for summary stats
    private MaterialTextView todayHours, weekAverage, daysStudied;

    public StudyAnalyticsFragment() {
        // Required empty public constructor
    }

    public static StudyAnalyticsFragment newInstance() {
        return new StudyAnalyticsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_study_analytics, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        weeklyBarChart = view.findViewById(R.id.weeklyBarChart);
        subjectPieChart = view.findViewById(R.id.subjectPieChart);
        goalValue = view.findViewById(R.id.goalValue);

        // Initialize new TextViews
        todayHours = view.findViewById(R.id.todayTimeText);
        weekAverage = view.findViewById(R.id.weeklyAvgText);
        daysStudied = view.findViewById(R.id.streakText);

        configureCharts();
        loadStudyDataFromFirestore();
        loadDailyGoal();
    }

    private void configureCharts() {
        // Bar Chart config
        weeklyBarChart.getDescription().setEnabled(false);
        weeklyBarChart.getLegend().setEnabled(false);
        weeklyBarChart.setFitBars(true);
        weeklyBarChart.setExtraBottomOffset(10f);
        weeklyBarChart.setTouchEnabled(true);
        weeklyBarChart.setDragEnabled(true);
        weeklyBarChart.setScaleEnabled(true);
        weeklyBarChart.animateY(1000);

        XAxis xAxis = weeklyBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondaryText));
        xAxis.setTextSize(12f);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int index = (int) value;
                return (index >= 0 && index < days.length) ? days[index] : "";
            }
        });

        YAxis leftAxis = weeklyBarChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondaryText));
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0fh", value);
            }
        });

        weeklyBarChart.getAxisRight().setEnabled(false);

        // Pie Chart config
        subjectPieChart.setUsePercentValues(true);
        subjectPieChart.getDescription().setEnabled(false);
        subjectPieChart.setDrawEntryLabels(false);
        subjectPieChart.setCenterText("Study Time");
        subjectPieChart.setCenterTextSize(14f);
        subjectPieChart.setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.primaryText));
        subjectPieChart.setHoleRadius(45f);
        subjectPieChart.setTransparentCircleRadius(50f);
        subjectPieChart.getLegend().setTextColor(ContextCompat.getColor(requireContext(), R.color.primaryText));
        subjectPieChart.getLegend().setTextSize(12f);
        subjectPieChart.animateY(1000);
    }

    private void loadStudyDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference sessionsRef = db.collection("Students");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Calculate start of current week (Sunday)
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
                    float[] dailyTotals = new float[7]; // Sun=0 ... Sat=6
                    Map<String, Float> subjectTotals = new HashMap<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Timestamp timestamp = doc.getTimestamp("dateSaved");
                        if (timestamp == null) continue;

                        long timeMillis = timestamp.toDate().getTime();
                        if (timeMillis < weekStartMillis || timeMillis > now) continue;

                        float focusTime = 0f;
                        if (doc.getDouble("focusTime") != null) {
                            focusTime = doc.getDouble("focusTime").floatValue();
                        }

                        String subject = doc.getString("subject");

                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(timeMillis);
                        int index = cal.get(Calendar.DAY_OF_WEEK) - 1; // Sunday = 0
                        dailyTotals[index] += focusTime;

                        if (subject != null) {
                            subjectTotals.put(subject,
                                    subjectTotals.getOrDefault(subject, 0f) + focusTime);
                        }
                    }

                    updateWeeklyBarChart(dailyTotals);
                    updateSubjectPieChart(subjectTotals);
                    updateSummaryStats(dailyTotals);  // <-- NEW: update the summary stats TextViews
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    private void updateWeeklyBarChart(float[] dailyTotals) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, dailyTotals[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Study Hours");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.primaryText));
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1fh", value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);

        weeklyBarChart.setData(barData);
        weeklyBarChart.invalidate();
    }

    private void updateSubjectPieChart(Map<String, Float> subjectTotals) {
        List<PieEntry> entries = new ArrayList<>();
        float total = 0f;
        for (float val : subjectTotals.values()) {
            total += val;
        }

        for (Map.Entry<String, Float> entry : subjectTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Subject Distribution");

        List<Integer> customColors = new ArrayList<>();
        customColors.add(Color.parseColor("#4CAF50")); // Green
        customColors.add(Color.parseColor("#FF9800")); // Orange
        customColors.add(Color.parseColor("#2196F3")); // Blue
        customColors.add(Color.parseColor("#F44336")); // Red
        customColors.add(Color.parseColor("#9C27B0")); // Purple
        customColors.add(Color.parseColor("#009688")); // Teal
        customColors.add(Color.parseColor("#FFEB3B")); // Yellow
        dataSet.setColors(customColors);

        dataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
        dataSet.setValueFormatter(new PercentFormatter(subjectPieChart));

        PieData pieData = new PieData(dataSet);
        subjectPieChart.setData(pieData);
        subjectPieChart.invalidate();
    }

    // NEW METHOD: Calculate and update summary stats TextViews
    private void updateSummaryStats(float[] dailyTotals) {
        Calendar calendar = Calendar.getInstance();
        int todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1; // Sunday = 0

        float todayHoursValue = dailyTotals[todayIndex];

        // Count days studied so far (non-zero entries)
        int daysStudiedCount = 0;
        int daysSoFar = todayIndex + 1; // Days since Sunday up to today

        float totalHoursSoFar = 0f;
        for (int i = 0; i < daysSoFar; i++) {
            if (dailyTotals[i] > 0) {
                daysStudiedCount++;
            }
            totalHoursSoFar += dailyTotals[i];
        }

        // Week average = total hours so far / days so far
        float weekAverageValue = daysSoFar > 0 ? (totalHoursSoFar / daysSoFar) : 0f;

        // Update UI with formatted values
        todayHours.setText(String.format(" %.2fh", todayHoursValue));
        weekAverage.setText(String.format(" %.2fh/day", weekAverageValue));
        daysStudied.setText(String.format(" %d days", daysStudiedCount));
    }

    private void loadDailyGoal() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Students").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Double dailyGoal = documentSnapshot.getDouble("goal");
                if (dailyGoal != null && goalValue != null) {
                    goalValue.setText(String.format("%.1fh/day", dailyGoal));
                }
            }
        }).addOnFailureListener(e -> {
            // Handle error
        });
    }
}
