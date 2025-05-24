package com.example.studytrackerapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class StudyAnalyticsFragment extends Fragment {

    private BarChart weeklyBarChart;
    private PieChart subjectPieChart;

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

        // Initialize charts
        weeklyBarChart = view.findViewById(R.id.weeklyBarChart);
        subjectPieChart = view.findViewById(R.id.subjectPieChart);

        setupWeeklyBarChart();
        setupSubjectPieChart();
    }

    private void setupWeeklyBarChart() {
        // Sample data - replace with your actual data
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, 2.5f)); // Monday
        entries.add(new BarEntry(1f, 3.0f)); // Tuesday
        entries.add(new BarEntry(2f, 1.5f)); // Wednesday
        entries.add(new BarEntry(3f, 4.0f)); // Thursday
        entries.add(new BarEntry(4f, 3.5f)); // Friday
        entries.add(new BarEntry(5f, 2.0f)); // Saturday
        entries.add(new BarEntry(6f, 3.0f)); // Sunday

        BarDataSet dataSet = new BarDataSet(entries, "Study Hours");
        dataSet.setColors(new int[] {
                ContextCompat.getColor(requireContext(), R.color.colorPrimary),
                ContextCompat.getColor(requireContext(), R.color.colorPrimaryLight),
                ContextCompat.getColor(requireContext(), R.color.colorAccent),
                ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark),
                ContextCompat.getColor(requireContext(), R.color.colorAccentLight),
                ContextCompat.getColor(requireContext(), R.color.colorPrimaryLight),
                ContextCompat.getColor(requireContext(), R.color.colorAccentDark)
        });
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.primaryText));
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);
        barData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1fh", value);
            }
        });

        // Configure chart appearance
        weeklyBarChart.setData(barData);
        weeklyBarChart.setFitBars(true);
        weeklyBarChart.getDescription().setEnabled(false);
        weeklyBarChart.getLegend().setEnabled(false);
        weeklyBarChart.setExtraBottomOffset(10f);

        // X-axis configuration
        XAxis xAxis = weeklyBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondaryText));
        xAxis.setTextSize(12f);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int index = (int) value;
                if (index >= 0 && index < days.length) {
                    return days[index];
                }
                return "";
            }
        });

        // Y-axis configuration
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
        weeklyBarChart.setTouchEnabled(true);
        weeklyBarChart.setDragEnabled(true);
        weeklyBarChart.setScaleEnabled(true);
        weeklyBarChart.animateY(1000);
        weeklyBarChart.invalidate();
    }

    private void setupSubjectPieChart() {
        // Sample data - replace with your actual data
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(12f, "Mathematics"));
        entries.add(new PieEntry(8f, "Science"));
        entries.add(new PieEntry(5f, "History"));
        entries.add(new PieEntry(7f, "Language"));
        entries.add(new PieEntry(4f, "Other"));

        PieDataSet dataSet = new PieDataSet(entries, "Subject Distribution");
        dataSet.setColors(new int[] {
                ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark),  // Math
                ContextCompat.getColor(requireContext(), R.color.colorAccent),      // Science
                ContextCompat.getColor(requireContext(), R.color.colorPrimaryLight), // History
                ContextCompat.getColor(requireContext(), R.color.colorAccentDark),   // Language
                ContextCompat.getColor(requireContext(), R.color.secondaryText)      // Other
        });
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1fh", value);
            }
        });

        PieData pieData = new PieData(dataSet);
        subjectPieChart.setData(pieData);
        subjectPieChart.setUsePercentValues(false);
        subjectPieChart.getDescription().setEnabled(false);
        subjectPieChart.setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.primaryText));
        subjectPieChart.setEntryLabelTextSize(12f);
        subjectPieChart.setCenterText("Study Time");
        subjectPieChart.setCenterTextSize(14f);
        subjectPieChart.setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.primaryText));
        subjectPieChart.setHoleRadius(45f);
        subjectPieChart.setTransparentCircleRadius(50f);
        subjectPieChart.setDrawEntryLabels(false);

        // Legend configuration
        subjectPieChart.getLegend().setTextColor(ContextCompat.getColor(requireContext(), R.color.primaryText));
        subjectPieChart.getLegend().setTextSize(12f);
        subjectPieChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        subjectPieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        subjectPieChart.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
        subjectPieChart.getLegend().setDrawInside(false);

        subjectPieChart.setRotationEnabled(true);
        subjectPieChart.setHighlightPerTapEnabled(true);
        subjectPieChart.animateY(1000);
        subjectPieChart.invalidate();
    }
}