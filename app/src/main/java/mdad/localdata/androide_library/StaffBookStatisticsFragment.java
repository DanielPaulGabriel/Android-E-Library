package mdad.localdata.androide_library;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StaffBookStatisticsFragment extends Fragment {
    private static final String ARG_BOOK_ID = "bookId";
    private int bookId;

    private TextView tvTotalMonthBorrows, tvTopBorrowingMonth;
    private BarChart barChart;
    private ImageButton btnBack;

    private static final String GET_BOOK_STATISTICS_URL = Constants.GET_BOOK_STATISTICS_URL;

    public StaffBookStatisticsFragment() {
        // Required empty public constructor
    }

    public static StaffBookStatisticsFragment newInstance(int bookId) {
        StaffBookStatisticsFragment fragment = new StaffBookStatisticsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOOK_ID, bookId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookId = getArguments().getInt(ARG_BOOK_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_staff_book_statistics, container, false);

        tvTotalMonthBorrows = rootView.findViewById(R.id.tvTotalMonthBorrows);
        tvTopBorrowingMonth = rootView.findViewById(R.id.tvTopBorrowingMonth);
        barChart = rootView.findViewById(R.id.barChart);
        btnBack = rootView.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v->requireActivity().getSupportFragmentManager().popBackStack());

        fetchBookStatistics();
        return rootView;
    }

    private void fetchBookStatistics() {
        String url = GET_BOOK_STATISTICS_URL + "?book_id=" + bookId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONObject statsObject = jsonObject.getJSONObject("stats");

                            // Update TextViews
                            tvTotalMonthBorrows.setText(
                                    "Total Borrows This Month: " + statsObject.getInt("totalBorrowsThisMonth")
                            );
                            tvTopBorrowingMonth.setText(
                                    "This Book Was Borrowed The Most In: " + statsObject.getString("topBorrowingMonth")
                            );

                            // Populate LineChart
                            JSONObject monthlyData = statsObject.getJSONObject("monthlyData");
                            setupBarChart(monthlyData);
                        } else {
                            Toast.makeText(getContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to parse data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error fetching statistics", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(requireContext()).add(request);
    }

    /*private void setupLineChart(JSONObject monthlyData) throws JSONException {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Iterator<String> keys = monthlyData.keys();
        int index = 0;
        while (keys.hasNext()) {
            String month = keys.next();
            labels.add(month);
            entries.add(new Entry(index, monthlyData.getInt(month)));
            index++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Books Borrowed");
        dataSet.setColor(getResources().getColor(R.color.dark_primary));
        dataSet.setValueTextSize(10f);
        dataSet.setCircleRadius(5f);
        dataSet.setCircleColor(getResources().getColor(R.color.dark_primary));

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        //xAxis.setValueFormatter((value, axis) -> labels.get((int) value));
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value; // Convert float to int
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index); // Safely retrieve the label
                } else {
                    return ""; // Return an empty string if index is out of bounds
                }
            }
        });

        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate(); // Refresh the chart
    }*/
    private void setupBarChart(JSONObject monthlyData) throws JSONException {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Iterator<String> keys = monthlyData.keys();
        int index = 0;
        while (keys.hasNext()) {
            String month = keys.next();
            labels.add(month);
            entries.add(new BarEntry(index, monthlyData.getInt(month))); // Use BarEntry for bar charts
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Books Borrowed");
        dataSet.setColor(getResources().getColor(R.color.dark_primary));
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.LTGRAY); // Adjust text color to match theme
        dataSet.setDrawValues(false); // Disable value labels

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.2f); // Set bar width

        barChart.setData(barData);

        // Configure X Axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                } else {
                    return "";
                }
            }
        });

        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.GRAY); // Light gray text color
        xAxis.setDrawGridLines(false); // Show grid lines
        //xAxis.setGridColor(Color.DKGRAY); // Darker grid lines for visibility

        // Configure Y Axis (Left)
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setTextColor(Color.GRAY);
        leftAxis.setDrawGridLines(false); // Show grid lines
        //leftAxis.setGridColor(Color.DKGRAY);

        // Disable right Y Axis
        barChart.getAxisRight().setEnabled(false);

        // Customize Legend
        barChart.getLegend().setTextColor(Color.GRAY);
        barChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        barChart.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
        barChart.getLegend().setDrawInside(false);

        // Add Marker
        ChartMarkerView marker = new ChartMarkerView(getContext());
        barChart.setMarker(marker);

        // Add extra padding to prevent clipping
        barChart.setExtraOffsets(10, 10, 50, 10);

        // Enable animations
        barChart.animateY(1500);

        // Remove chart description
        barChart.getDescription().setEnabled(false);

        barChart.setFitBars(true); // Makes the bars fit nicely in the chart

        // Refresh the chart
        barChart.invalidate();
    }


}
