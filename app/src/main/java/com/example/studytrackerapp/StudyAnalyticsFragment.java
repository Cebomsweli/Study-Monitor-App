package com.example.studytrackerapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

public class StudyAnalyticsFragment extends Fragment {

    private BarChart studyTimeChart;

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
        studyTimeChart = view.findViewById(R.id.studyTimeChart);
        setupBarChart();
    }

    private void setupBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, 2.5f)); // Monday
        entries.add(new BarEntry(1f, 3.0f)); // Tuesday
        entries.add(new BarEntry(2f, 1.5f)); // Wednesday
        entries.add(new BarEntry(3f, 4.0f)); // Thursday
        entries.add(new BarEntry(4f, 3.5f)); // Friday
        entries.add(new BarEntry(5f, 2.0f)); // Saturday
        entries.add(new BarEntry(6f, 3.0f)); // Sunday

        BarDataSet dataSet = new BarDataSet(entries, "Study Time (hrs)");
        dataSet.setColor(Color.parseColor("#6200EE")); // Customize color

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        studyTimeChart.setData(barData);
        studyTimeChart.setFitBars(true);
        studyTimeChart.getDescription().setEnabled(false);

        XAxis xAxis = studyTimeChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        studyTimeChart.invalidate(); // Refresh chart
    }
}
